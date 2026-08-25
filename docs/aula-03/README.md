# Aula 3 - JUnit 5, lifecycle e isolamento

Tempo sugerido: 3 a 5 horas, incluindo experimentacao e exercicios.
Pré-requisito: Aula 2 concluída.

## Objetivos

Ao terminar esta aula, voce devera conseguir:

- explicar o lifecycle do JUnit usado pela suite;
- reutilizar `Playwright` e `Browser` sem compartilhar cookies ou storage;
- criar um `BrowserContext` e uma `Page` novos para cada teste;
- executar a mesma classe em Chromium, Firefox e WebKit por propriedade;
- alternar headless/headed sem modificar o codigo;
- configurar URL e timeouts externamente;
- garantir teardown mesmo quando um teste falha.

## 1. O problema que vamos resolver

Na Aula 2, cada metodo repetia todo o bootstrap:

```text
Teste 1 -> Playwright -> Browser -> Context -> Page
Teste 2 -> Playwright -> Browser -> Context -> Page
Teste 3 -> Playwright -> Browser -> Context -> Page
```

Isso funciona, mas iniciar um browser para cada teste e caro. O extremo oposto,
compartilhar uma unica `Page`, cria dependencia de ordem, cookies vazando e
falhas em cascata.

O equilibrio recomendado e:

```text
Classe de teste
  Playwright (compartilhado)
  Browser    (compartilhado)

Cada teste
  BrowserContext (novo e isolado)
  Page           (nova dentro do contexto)
```

Contextos sao leves e nao compartilham cookies, local storage ou session
storage. Fechar o contexto descarta a sessao inteira; nao precisamos manter uma
lista crescente de comandos de limpeza.

## 2. Lifecycle do JUnit 5

Usaremos quatro hooks:

| Hook | Frequencia | Responsabilidade |
|---|---|---|
| `@BeforeAll` | uma vez por classe | ler configuracao e abrir Playwright/Browser |
| `@BeforeEach` | antes de cada teste | criar BrowserContext e Page |
| `@AfterEach` | depois de cada teste | fechar o BrowserContext |
| `@AfterAll` | uma vez por classe | fechar Browser e Playwright |

Por padrao, o JUnit cria uma instancia nova da classe para cada metodo de teste.
Vamos usar:

```java
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
```

Assim existe uma instancia da classe durante toda a execucao e `@BeforeAll` e
`@AfterAll` podem ser metodos de instancia. Isso deixa o exemplo sem campos
`static`, mas cria uma responsabilidade: os campos mutaveis por teste devem ser
substituidos em todo `@BeforeEach` e descartados em todo `@AfterEach`.

## 3. Ownership dos recursos

Todo recurso deve ter um dono claro:

- `@BeforeAll` cria `Playwright` e `Browser`; `@AfterAll` fecha ambos.
- `@BeforeEach` cria `BrowserContext` e `Page`; `@AfterEach` fecha o contexto.
- fechar o contexto tambem fecha as paginas que pertencem a ele.

O teardown deve aceitar campos nulos. Se uma inicializacao falhar pela metade,
nao queremos que o fechamento esconda o erro original com outro
`NullPointerException`.

```java
@AfterEach
void fecharContexto() {
  if (context != null) {
    context.close();
    context = null;
    page = null;
  }
}
```

Zerar as referencias nao e o que fecha os recursos; `close()` faz isso. Os
`null` apenas deixam o estado da fixture explicito e evitam reutilizacao
acidental.

## 4. Configuracao externa

Leia estas duas classes antes de usar:

- `src/test/java/br/com/curso/playwright/aula03/support/TestConfig.java`
- `src/test/java/br/com/curso/playwright/aula03/support/BrowserFactory.java`

`TestConfig` transforma propriedades da JVM em valores validados. A suite nao
precisa ser recompilada para mudar de browser ou modo de execucao.

Valores padrao:

| Propriedade | Padrao | Funcao |
|---|---|---|
| `baseUrl` | `https://www.saucedemo.com/` | endereco da aplicacao |
| `browser` | `chromium` | `chromium`, `firefox` ou `webkit` |
| `headless` | `true` | executar sem interface grafica |
| `timeout` | `10000` | timeout de acoes, navegacao e assertions em ms |

Exemplos:

```powershell
.\mvn-local.ps1 -Dtest=LifecycleJUnitDemonstracaoTest test
.\mvn-local.ps1 -Dtest=LifecycleJUnitDemonstracaoTest -Dheadless=false test
.\mvn-local.ps1 -Dtest=LifecycleJUnitDemonstracaoTest -Dbrowser=firefox test
```

No codigo, o contexto recebe `baseURL`, permitindo navegar com caminho relativo:

```java
context = browser.newContext(
    new Browser.NewContextOptions().setBaseURL(config.baseUrl()));
page = context.newPage();
page.navigate("/");
```

## 5. Tres timeouts diferentes

```java
context.setDefaultTimeout(config.timeoutMs());
context.setDefaultNavigationTimeout(config.timeoutMs());
PlaywrightAssertions.setDefaultAssertionTimeout(config.timeoutMs());
```

- `setDefaultTimeout`: acoes e operacoes gerais de locators.
- `setDefaultNavigationTimeout`: navegacoes.
- `setDefaultAssertionTimeout`: retry das assertions web-first.

Um timeout nao deve ser usado para esconder lentidao indefinidamente. Ele e um
limite para uma condicao observavel, nao uma pausa fixa.

## 6. Demonstracao executavel

Leia:

`src/test/java/br/com/curso/playwright/aula03/LifecycleJUnitDemonstracaoTest.java`

Antes de executar, identifique:

1. quais campos permanecem durante a classe;
2. quais campos mudam antes de cada teste;
3. onde `data-test` e configurado uma unica vez;
4. por que os testes nao precisam conhecer o browser escolhido;
5. como o segundo teste comeca desautenticado mesmo que o outro faca login.

Execute em headless:

```powershell
.\mvn-local.ps1 -Dtest=LifecycleJUnitDemonstracaoTest test
```

Depois execute headed e observe que nao houve alteracao no Java:

```powershell
.\mvn-local.ps1 -Dtest=LifecycleJUnitDemonstracaoTest -Dheadless=false test
```

## 7. Exercicios

Implemente:

`src/test/java/br/com/curso/playwright/aula03/LifecycleEConfiguracaoExercicioTest.java`

Remova `@Disabled` quando o lifecycle estiver completo.

### Parte 1 - Monte a fixture

Crie campos para `TestConfig`, `Playwright`, `Browser`, `BrowserContext` e
`Page`. Depois implemente:

- `@BeforeAll`: le configuracao, cria Playwright, configura `data-test` e
  assertion timeout, e abre o browser pela `BrowserFactory`;
- `@BeforeEach`: cria contexto com `baseURL`, aplica os dois timeouts e cria a
  pagina;
- `@AfterEach`: fecha o contexto de maneira segura;
- `@AfterAll`: fecha browser e Playwright de maneira segura;
- `@TestInstance(PER_CLASS)`: permite hooks de classe nao estaticos.

### Parte 2 - Escreva tres testes independentes

1. login valido: autentique `standard_user` e verifique o titulo `Products`;
2. login bloqueado: autentique `locked_out_user` e verifique a mensagem de erro;
3. sessao limpa: abra `/` e verifique que o botao `Login` esta visivel e a URL
   continua na pagina inicial.

Cada teste deve usar os campos `page` e `config` preparados pelos hooks. Nenhum
teste pode chamar `Playwright.create()`, `browser.newContext()` ou `close()`.

### Parte 3 - Prove a configuracao

Execute a classe:

```powershell
.\mvn-local.ps1 -Dtest=LifecycleEConfiguracaoExercicioTest test
.\mvn-local.ps1 -Dtest=LifecycleEConfiguracaoExercicioTest -Dheadless=false test
.\mvn-local.ps1 -Dtest=LifecycleEConfiguracaoExercicioTest -Dbrowser=firefox test
```

Depois execute um teste individual. Ele deve continuar passando sem depender
dos demais:

```powershell
.\mvn-local.ps1 '-Dtest=LifecycleEConfiguracaoExercicioTest#deveIniciarComSessaoLimpa' test
```

## 8. Desafio de diagnostico

Este desafio inclui **experimentacao orientada**. Execute propositalmente:

```powershell
.\mvn-local.ps1 -Dtest=LifecycleEConfiguracaoExercicioTest -Dbrowser=safari test
```

Leia a falha e responda:

- ela ocorre antes, durante ou depois dos testes?
- qual classe rejeita o valor?
- o browser chegou a ser iniciado?
- a mensagem informa os valores aceitos?

Depois volte para um browser valido. Nao altere o codigo para aceitar `safari`.

## O que ainda nao faremos

- coleções, ordenação e parametrização entram na Aula 4;
- Page Objects e a extensão JUnit entram na Aula 7;
- API e estado autenticado entram na Aula 8;
- paralelismo e segurança entre threads entram na Aula 10;
- evidências e Allure entram na Aula 11;
- nossa extensão JUnit encapsulará o lifecycle depois que esse fluxo estiver
  completamente compreendido; nesta aula ele deve permanecer visível.

## Criterios para revisao

- apenas um `Playwright` e um `Browser` sao criados para a classe;
- cada teste recebe novo `BrowserContext` e nova `Page`;
- os tres testes passam juntos e isoladamente;
- Chromium headless/headed e Firefox funcionam sem editar o Java;
- URL, browser, headless e timeout nao estao hardcoded na classe de teste;
- hooks fecham os recursos pelos quais sao responsaveis;
- nao existe dependencia de ordem, espera fixa, Page Object ou classe base;
- as seis perguntas da Aula 3 foram respondidas em `docs/PROGRESSO.md`.

## Leitura oficial

- [Test runners no Playwright Java](https://playwright.dev/java/docs/test-runners)
- [Isolamento por BrowserContext](https://playwright.dev/java/docs/browser-contexts)
- [PlaywrightAssertions](https://playwright.dev/java/docs/api/class-playwrightassertions)
- [Lifecycle de instancias no JUnit](https://docs.junit.org/current/user-guide/)

## Correção no formato completo

```powershell
.\course.ps1 exercise 03
.\course.ps1 validate 03
.\course.ps1 solution 03
```

Rubrica: um runtime/browser por classe, contexto/página por teste, configuração
externa, teardown tolerante a inicialização parcial e execução isolada. Só
consulte a solução quando conseguir desenhar o ownership dos cinco objetos.
