# Aula 1 - Fundamentos e primeiro teste

Tempo sugerido: 4 a 6 horas, incluindo experimentacao e exercicio.
Pré-requisito: Java e JUnit básicos; nenhum conhecimento de Playwright.

## Objetivos

Ao terminar esta aula, voce devera conseguir:

- explicar a hierarquia `Playwright -> Browser -> BrowserContext -> Page`;
- abrir e fechar esses recursos na ordem correta;
- navegar ate o SauceDemo em Chromium;
- alternar entre execucao headless e headed;
- implementar um login simples sem `Thread.sleep`;
- explicar por que Playwright nao usa o modelo tradicional de WebDriver.

## 1. Mudanca de modelo mental

No Selenium, normalmente voce pensa primeiro no `WebDriver`: ele representa a
sessao controlada e o ponto central para localizar elementos e navegar.

No Playwright, as responsabilidades ficam mais explicitas:

```text
Playwright
  -> Browser
       -> BrowserContext
            -> Page
                 -> Locator
```

- `Playwright` inicia o runtime e oferece os motores Chromium, Firefox e WebKit.
- `Browser` e o processo do navegador.
- `BrowserContext` e uma sessao isolada, semelhante a um perfil anonimo leve.
- `Page` representa uma aba.
- `Locator` representa uma consulta que sera resolvida no momento da acao ou da
  verificacao; ele nao e uma fotografia antiga do elemento.

A unidade de isolamento que usaremos nos testes sera o `BrowserContext`. Nas
aulas posteriores, cada teste recebera um contexto novo, enquanto recursos mais
caros poderao ser reutilizados com seguranca.

## 2. Selenium versus Playwright

| Situacao | Selenium | Playwright |
|---|---|---|
| Processo principal | `WebDriver` | `Browser` |
| Nova sessao isolada | Geralmente outro driver/perfil | Novo `BrowserContext` |
| Nova aba | `WindowHandle` | `Page` |
| Referencia ao elemento | `WebElement` | `Locator` reutilizavel |
| Espera antes de agir | Frequentemente `WebDriverWait` | Auto-wait integrado as acoes |
| Browsers suportados | Driver por browser | Binarios compativeis gerenciados pela ferramenta |

Auto-wait nao significa esperar qualquer regra de negocio. Significa que a acao
como `click()` verifica condicoes de acionabilidade antes de interagir. Na Aula 2
vamos separar auto-wait, espera por navegacao e assertions que aguardam estado.

## 3. APIs mínimas para o primeiro login

A Aula 2 aprofundará locators. Antes do primeiro exercício, porém, você precisa
conhecer exatamente as três formas usadas nele.

### Campo pelo placeholder

O placeholder é o texto de ajuda exibido dentro do campo:

```html
<input placeholder="Username">
```

```java
page.getByPlaceholder("Username").fill("standard_user");
```

`getByPlaceholder` devolve um `Locator`; `fill` aguarda o campo ficar apto e
substitui seu conteúdo.

### Botão pelo papel e nome acessível

```java
page.getByRole(
    AriaRole.BUTTON,
    new Page.GetByRoleOptions().setName("Login"))
    .click();
```

Leia como: “clique no controle com papel de botão e nome Login”. Role não é a
tag. O login do SauceDemo é um `<input type="submit" value="Login">`; o HTML
atribui implicitamente a ele o papel `button`, e `value` fornece o nome
acessível. Portanto `AriaRole.BUTTON` é correto mesmo sem uma tag `<button>`.

Importe:

```java
import com.microsoft.playwright.options.AriaRole;
```

### Estado pelo contrato data-test

O SauceDemo usa `data-test`, enquanto `getByTestId` procura `data-testid` por
padrão. Configure o atributo antes de criar os locators:

```java
playwright.selectors().setTestIdAttribute("data-test");
```

Depois:

```java
assertThat(page.getByTestId("title")).hasText("Products");
```

O import da assertion é:

```java
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
```

`hasText` tenta novamente até o texto aparecer ou o timeout terminar. Não use
`Thread.sleep`.

## 4. Leia a demonstracao

Abra:

`src/test/java/br/com/curso/playwright/aula01/PrimeiroContatoPlaywrightTest.java`

Antes de executar, tente prever:

1. Qual objeto nasce primeiro?
2. Qual recurso precisa existir antes de criar uma `Page`?
3. O que aconteceria se o contexto nao fosse fechado?
4. Em que linha o navegador passa a acessar uma aplicacao externa?

Agora instale o Chromium e rode o teste:

```powershell
.\mvn-local.ps1 compile exec:java '-Dexec.mainClass=com.microsoft.playwright.CLI' '-Dexec.args=install chromium'
.\mvn-local.ps1 test
```

Repita visualmente:

```powershell
.\mvn-local.ps1 test -Dheadless=false
```

Experimente tambem passar uma URL incorreta e observe a falha. Depois reverta a
mudanca. O objetivo e aprender a ler a mensagem, nao apenas obter verde.

## 5. Exercicio guiado - login valido

Implemente o teste desabilitado em:

`src/test/java/br/com/curso/playwright/aula01/LoginSauceDemoExercicioTest.java`

Dados publicos exibidos pelo proprio SauceDemo:

- usuario: `standard_user`
- senha: `secret_sauce`

Passos esperados:

1. remova `@Disabled`;
2. crie `Playwright`, Chromium, contexto e pagina;
3. navegue ate `https://www.saucedemo.com/`;
4. localize os campos pelo placeholder visivel;
5. preencha usuario e senha;
6. clique no botao de login pelo papel e nome acessivel;
7. verifique que a URL termina com `/inventory.html`;
8. feche contexto e browser mesmo se a verificacao falhar.

Restricoes desta tentativa:

- nao use CSS, XPath, `Thread.sleep`, `waitForTimeout` ou JavaScript;
- nao copie o lifecycle para uma classe base ainda;
- nao crie Page Object nesta aula;
- use uma assertion do Playwright, nao apenas uma assertion de `String` do JUnit.

## 6. Dicas graduais

Leia uma dica apenas quando estiver realmente bloqueado.

<details>
<summary>Dica 1 - Localizacao</summary>

A classe `Page` possui APIs como `getByPlaceholder` e `getByRole`. Para o botao,
voce precisara do enum `AriaRole`.
</details>

<details>
<summary>Dica 2 - Assertion</summary>

As assertions web-first sao acessadas pelo import estatico de
`PlaywrightAssertions.assertThat`.
</details>

<details>
<summary>Dica 3 - Fechamento</summary>

Use `try`/`finally` para fechar `BrowserContext` e `Browser` antes de sair do
`try` que controla `Playwright`.
</details>

## 7. Desafio independente

Sem criar outro teste, adicione uma verificacao de que o titulo visivel da tela
de inventario e `Products`. Escolha um locator acessivel ou o atributo
`data-test`; esteja preparado para explicar a escolha.

Nao automatize usuario bloqueado ainda. Esse cenario sera usado para aprender
strictness, mensagens de erro e assertions na Aula 2.

## Criterios para revisao

- o teste passa em headless e headed;
- nao existe espera fixa;
- o locator comunica a intencao do elemento;
- a verificacao usa uma assertion com espera integrada;
- os recursos sao fechados mesmo em caso de falha;
- voce consegue responder as quatro perguntas de `docs/PROGRESSO.md`.

Quando terminar, peça a revisao do arquivo e inclua suas respostas. Eu nao vou
apenas dizer se passou: vou avaliar as decisoes e solicitar uma refatoracao antes
de liberar a Aula 2.

## Correção no formato completo

```powershell
.\course.ps1 exercise 01
.\course.ps1 validate 01
.\course.ps1 solution 01
```

Consulte o gabarito apenas depois da tentativa. A rubrica desta aula é:
lifecycle fechado em falhas, locator intencional, assertion web-first, ausência
de espera fixa e capacidade de explicar cada objeto criado.

Leituras: [instalação](https://playwright.dev/java/docs/intro),
[browsers](https://playwright.dev/java/docs/browsers) e
[BrowserContext](https://playwright.dev/java/docs/browser-contexts).
