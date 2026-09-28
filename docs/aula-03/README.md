# Aula 3 - JUnit 5, lifecycle e isolamento

Tempo sugerido: 3 a 5 horas, incluindo experimentacao e exercicios.  
Pre-requisito: Aula 2 concluida.

## Objetivos

Ao terminar esta aula, voce devera conseguir:

- explicar o lifecycle do JUnit usado pela suite;
- reutilizar `Playwright` e `Browser` sem compartilhar cookies ou storage;
- criar um `BrowserContext` e uma `Page` novos para cada teste;
- executar os testes no Microsoft Edge instalado na maquina;
- alternar entre execucao visivel e headless sem modificar o codigo;
- configurar URL e timeouts externamente;
- garantir teardown mesmo quando um teste falha.

## 1. O problema que vamos resolver

Na Aula 2, cada metodo repetia todo o bootstrap:

```text
Teste 1 -> Playwright -> Browser -> Context -> Page
Teste 2 -> Playwright -> Browser -> Context -> Page
Teste 3 -> Playwright -> Browser -> Context -> Page
```

Isso funciona, mas iniciar um browser para cada teste e caro. O extremo oposto, compartilhar uma unica `Page`, cria dependencia de ordem, vazamento de cookies e falhas em cascata.

O equilibrio recomendado e:

```text
Classe de teste
  Playwright (compartilhado)
  Browser    (compartilhado)

Cada teste
  BrowserContext (novo e isolado)
  Page           (nova dentro do contexto)
```

Contextos sao leves e nao compartilham cookies, local storage ou session storage. Fechar o contexto descarta a sessao inteira, sem exigir uma lista crescente de comandos de limpeza.

## 2. Lifecycle do JUnit 5

Usaremos quatro hooks:

| Hook | Frequencia | Responsabilidade |
|---|---|---|
| `@BeforeAll` | Uma vez por classe | Ler a configuracao e abrir Playwright e Browser |
| `@BeforeEach` | Antes de cada teste | Criar BrowserContext e Page |
| `@AfterEach` | Depois de cada teste | Fechar o BrowserContext |
| `@AfterAll` | Uma vez por classe | Fechar Browser e Playwright |

Por padrao, o JUnit cria uma instancia nova da classe para cada metodo de teste. Nesta aula, usaremos:

```java
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
```

Assim, existe uma instancia da classe durante toda a execucao, e `@BeforeAll` e `@AfterAll` podem ser metodos de instancia. Os campos mutaveis de cada teste devem ser substituidos em todo `@BeforeEach` e descartados em todo `@AfterEach`.

## 3. Ownership dos recursos

Todo recurso deve ter um responsavel claro:

- `@BeforeAll` cria `Playwright` e `Browser`; `@AfterAll` fecha ambos;
- `@BeforeEach` cria `BrowserContext` e `Page`; `@AfterEach` fecha o contexto;
- fechar o contexto tambem fecha as paginas que pertencem a ele.

O teardown deve aceitar campos nulos. Se uma inicializacao falhar parcialmente, o fechamento nao deve esconder o erro original com outro `NullPointerException`.

```java
@AfterEach
void fecharContexto() {
  try {
    if (context != null) {
      context.close();
    }
  } finally {
    context = null;
    page = null;
  }
}
```

Zerar as referencias nao fecha os recursos. O fechamento e realizado por `close()`. Os valores `null` apenas tornam o estado da fixture explicito e evitam reutilizacao acidental.

## 4. Configuracao externa

Leia estas classes antes de utilizar a fixture:

- `src/test/java/br/com/curso/playwright/aula03/support/TestConfig.java`
- `src/test/java/br/com/curso/playwright/aula03/support/BrowserFactory.java`

`TestConfig` transforma propriedades da JVM em valores validados. A suite nao precisa ser recompilada para alterar o modo de execucao, a URL ou os timeouts.

O Microsoft Edge instalado na maquina e o navegador padrao e unico navegador utilizado nesta etapa do curso.

Valores padrao:

| Propriedade | Padrao | Funcao |
|---|---|---|
| `baseUrl` | `https://www.saucedemo.com/` | Endereco da aplicacao |
| `headless` | `false` | Define se o navegador executa sem interface grafica |
| `timeout` | `10000` | Timeout de acoes, navegacao e assertions em milissegundos |

No codigo, o contexto recebe `baseURL`, permitindo navegar com um caminho relativo:

```java
context = browser.newContext(
    new Browser.NewContextOptions()
        .setBaseURL(config.baseUrl())
);

page = context.newPage();
page.navigate("/");
```

## 5. Tres timeouts diferentes

```java
context.setDefaultTimeout(config.timeoutMs());
context.setDefaultNavigationTimeout(config.timeoutMs());
PlaywrightAssertions.setDefaultAssertionTimeout(config.timeoutMs());
```

- `setDefaultTimeout`: acoes e operacoes gerais com locators;
- `setDefaultNavigationTimeout`: operacoes de navegacao;
- `setDefaultAssertionTimeout`: retry das assertions web-first.

Um timeout nao deve ser utilizado para esconder lentidao indefinidamente. Ele representa o limite para uma condicao observavel, nao uma pausa fixa.

## 6. Demonstracao executavel

Leia:

`src/test/java/br/com/curso/playwright/aula03/LifecycleJUnitDemonstracaoTest.java`

Antes de executar, identifique:

1. quais campos permanecem durante toda a classe;
2. quais campos mudam antes de cada teste;
3. onde `data-test` e configurado uma unica vez;
4. por que os testes nao precisam conhecer os detalhes de inicializacao do Microsoft Edge;
5. como o segundo teste comeca desautenticado mesmo que o outro realize login.

Execute com o Microsoft Edge visivel:

```powershell
.\course.ps1 demo 03
```

Execute sem exibir o navegador:

```powershell
.\course.ps1 demo 03 -Headless
```

Nos dois casos, nenhuma modificacao no codigo Java e necessaria.

## 7. Exercicios

Implemente:

`src/test/java/br/com/curso/playwright/aula03/LifecycleEConfiguracaoExercicioTest.java`

Remova `@Disabled` quando o lifecycle estiver completo.

### Parte 1 - Monte a fixture

Crie campos para `TestConfig`, `Playwright`, `Browser`, `BrowserContext` e `Page`. Depois implemente:

- `@BeforeAll`: le a configuracao, cria Playwright, configura `data-test` e o timeout de assertions, e abre o Edge pela `BrowserFactory`;
- `@BeforeEach`: cria o contexto com `baseURL`, aplica os timeouts e cria a pagina;
- `@AfterEach`: fecha o contexto de maneira segura;
- `@AfterAll`: fecha Browser e Playwright de maneira segura;
- `@TestInstance(PER_CLASS)`: permite hooks de classe nao estaticos.

### Parte 2 - Escreva tres testes independentes

1. Login valido: autentique `standard_user` e verifique o titulo `Products`.
2. Login bloqueado: autentique `locked_out_user` e verifique a mensagem de erro.
3. Sessao limpa: abra `/` e verifique que o botao `Login` esta visivel e que a URL continua na pagina inicial.

Cada teste deve utilizar os campos `page` e `config` preparados pelos hooks. Nenhum teste deve chamar `Playwright.create()`, `browser.newContext()` ou `close()`.

### Parte 3 - Prove a configuracao

Execute a classe com o Edge visivel:

```powershell
.\course.ps1 exercise 03
```

Execute em modo headless:

```powershell
.\course.ps1 exercise 03 -Headless
```

Depois, execute um teste individual. Ele deve continuar passando sem depender dos demais:

```powershell
.\mvn-local.ps1 test '-Dtest=LifecycleEConfiguracaoExercicioTest#deveIniciarComSessaoLimpa' -Dheadless=false
```

## 8. Desafio de diagnostico

Execute propositalmente com um valor invalido para `headless`:

```powershell
.\mvn-local.ps1 test -Dtest=LifecycleEConfiguracaoExercicioTest -Dheadless=talvez
```

Leia a falha e responda:

- ela ocorre antes, durante ou depois dos testes?
- qual classe rejeita o valor?
- o Edge chegou a ser iniciado?
- a mensagem informa os valores aceitos?

Depois, volte a utilizar `true` ou `false`. Nao altere o codigo para aceitar outros valores.

## O que ainda nao faremos

- colecoes, ordenacao e parametrizacao entram na Aula 4;
- Page Objects e a extensao JUnit entram na Aula 7;
- API e estado autenticado entram na Aula 8;
- paralelismo e seguranca entre threads entram na Aula 10;
- evidencias e Allure entram na Aula 11;
- a extensao JUnit encapsulara o lifecycle depois que esse fluxo estiver completamente compreendido; nesta aula, ele deve permanecer visivel.

## Criterios para revisao

- apenas um `Playwright` e um `Browser` sao criados para a classe;
- cada teste recebe um novo `BrowserContext` e uma nova `Page`;
- os tres testes passam juntos e isoladamente;
- o Microsoft Edge executa de forma visivel e headless sem alteracao no Java;
- URL, headless e timeout sao configurados externamente;
- o canal do Microsoft Edge permanece centralizado na `BrowserFactory`;
- cada hook fecha os recursos pelos quais e responsavel;
- o teardown aceita campos nulos e inicializacao parcial;
- nao existe dependencia de ordem, espera fixa, Page Object ou classe base;
- as perguntas da Aula 3 foram respondidas em `docs/PROGRESSO.md`.

## Leitura oficial

- [Test runners no Playwright Java](https://playwright.dev/java/docs/test-runners)
- [Isolamento por BrowserContext](https://playwright.dev/java/docs/browser-contexts)
- [PlaywrightAssertions](https://playwright.dev/java/docs/api/class-playwrightassertions)
- [Lifecycle de instancias no JUnit](https://docs.junit.org/current/user-guide/)

## Correcao no formato completo

```powershell
.\course.ps1 exercise 03
.\course.ps1 validate 03
.\course.ps1 solution 03
```

Rubrica: um runtime e um browser por classe, um contexto e uma pagina por teste, configuracao externa, teardown tolerante a inicializacao parcial e execucao isolada. Consulte a solucao apenas depois de conseguir representar o ownership dos cinco objetos.
