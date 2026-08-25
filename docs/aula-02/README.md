# Aula 2 - Locators, auto-wait e assertions

Tempo sugerido: 4 a 6 horas, incluindo experimentacao e exercicios.
Pré-requisito: Aula 1 concluída.

## Objetivos

Ao terminar esta aula, voce devera conseguir:

- reconstruir e explicar o bootstrap basico do Playwright;
- escolher locators pela intencao, e nao apenas por conveniencia;
- configurar o `data-test` usado pelo SauceDemo;
- explicar e diagnosticar strictness;
- diferenciar auto-wait de retry de assertion;
- testar erros de login sem `Thread.sleep` ou verificacoes imediatas frageis.

## 1. Recapitulacao: bootstrap desde o zero

Considere o fluxo minimo:

```java
try (Playwright playwright = Playwright.create()) {
  Browser browser = playwright.chromium().launch();
  BrowserContext context = browser.newContext();

  try {
    Page page = context.newPage();
    page.navigate("https://www.saucedemo.com/");
  } finally {
    context.close();
    browser.close();
  }
}
```

Leia de fora para dentro:

1. `Playwright.create()` inicia o runtime que conversa com os browsers.
2. `playwright.chromium()` escolhe um motor; ainda nao abre um browser.
3. `launch()` inicia o processo do browser.
4. `newContext()` cria uma sessao isolada, semelhante a um perfil anonimo.
5. `newPage()` cria uma aba dentro dessa sessao.
6. `navigate()` e a primeira interacao com a aplicacao.
7. o contexto e fechado antes do browser para encerrar sua sessao de forma
   deterministica; ao fim, `Playwright.close()` e chamado pelo try-with-resources.

Um `BrowserContext` pode conter varias `Page`. Duas paginas do mesmo contexto
compartilham cookies e storage; paginas de contextos diferentes ficam isoladas.

Nesta aula ainda repetiremos esse lifecycle. A Aula 3 mostrara como encapsula-lo
com JUnit 5 sem esconder o que acontece.

## 2. O que e um Locator

Um `Locator` descreve como encontrar um elemento. Ele nao guarda uma referencia
congelada como voce pode imaginar ao trabalhar com `WebElement`.

```java
Locator loginButton = page.getByRole(
    AriaRole.BUTTON,
    new Page.GetByRoleOptions().setName("Login")
);

loginButton.click();
```

O elemento e procurado novamente quando a acao acontece. Se a pagina renderizar
outro botao correspondente entre a criacao do locator e o clique, o locator usa
o estado atual do DOM.

## 3. Ordem de preferencia dos locators

Nao existe um locator universalmente melhor, mas existe uma boa heuristica:

1. `getByRole` com nome acessivel para botoes, links, headings e controles;
2. `getByLabel`, `getByPlaceholder`, `getByText` ou `getByAltText` quando
   representam o que o usuario percebe;
3. `getByTestId` quando existe um contrato explicito para automacao;
4. CSS para estruturas que nao possuem um contrato melhor;
5. XPath apenas quando houver uma justificativa concreta.

### Por role e nome acessivel

Importe o enum:

```java
import com.microsoft.playwright.options.AriaRole;
```

Depois expresse o elemento como o usuario o percebe:

```java
page.getByRole(
    AriaRole.BUTTON,
    new Page.GetByRoleOptions().setName("Login")
).click();
```

`BUTTON` e o papel. `Login` e o nome acessivel. Um `<button>Login</button>` e um
botao por semantica implicita; um `input type="submit" value="Login"` tambem pode
ser percebido dessa forma.

Isso é diferente de procurar um atributo literal. Estes elementos têm o mesmo
papel calculado pelo navegador:

```html
<button>Login</button>
<input type="submit" value="Login">
<button aria-label="Login"><img src="entrar.svg"></button>
```

Na árvore de acessibilidade, os três são apresentados como `button "Login"`.
Por isso `getByRole(BUTTON, name=Login)` encontra todos, enquanto o XPath
`//*[@role='button']` encontraria apenas elementos que possuem explicitamente
esse atributo. Não traduza role para XPath literal.

O locator falharia se o elemento fosse apenas `<div onclick="...">Login</div>`
sem semântica de botão. Essa falha pode revelar um problema real de
acessibilidade, não apenas um problema do teste.

### Por test ID no SauceDemo

Por padrao, `getByTestId("title")` procura `data-testid="title"`. O SauceDemo
usa `data-test="title"`, portanto precisamos declarar esse contrato uma vez:

```java
playwright.selectors().setTestIdAttribute("data-test");
```

Agora podemos escrever:

```java
assertThat(page.getByTestId("title")).hasText("Products");
```

Um test ID e uma boa escolha quando o texto varia, nao existe semantica
acessivel suficientemente especifica ou a equipe decidiu manter um contrato de
automacao. Ele nao deve substituir automaticamente todos os locators acessiveis.

## 4. Strictness

Acoes que exigem um unico elemento falham quando o locator encontra varios.
Considere dois botoes chamados `Salvar`:

```html
<section aria-label="Cadastro"><button>Salvar</button></section>
<section aria-label="Preferencias"><button>Salvar</button></section>
```

Este locator e ambiguo:

```java
page.getByRole(
    AriaRole.BUTTON,
    new Page.GetByRoleOptions().setName("Salvar")
).click();
```

Nao corrija automaticamente com `.first()`. Primeiro pergunte qual botao o
teste realmente quer. Uma solucao intencional e limitar a busca a uma regiao:

```java
Locator cadastro = page.getByRole(
    AriaRole.REGION,
    new Page.GetByRoleOptions().setName("Cadastro")
);

cadastro.getByRole(
    AriaRole.BUTTON,
    new Locator.GetByRoleOptions().setName("Salvar")
).click();
```

Strictness transforma ambiguidade silenciosa em uma falha explicavel.

## 5. Auto-wait nao e assertion

Antes de `click()`, o Playwright espera o elemento satisfazer condicoes de
acionabilidade, como estar visivel, estavel, habilitado e apto a receber eventos.
Isso e auto-wait da acao.

Uma assertion web-first repete sua verificacao ate o estado esperado aparecer
ou o timeout terminar:

```java
assertThat(page.getByTestId("error")).hasText("Mensagem esperada");
```

Evite esta forma para estados que ainda podem mudar:

```java
assertTrue(page.getByTestId("error").isVisible());
```

`isVisible()` devolve um booleano observado naquele instante. A assertion
`assertThat(...).isVisible()` possui retry e produz uma mensagem de falha mais
rica.

Auto-wait tambem nao conhece sua regra de negocio. Um clique pode terminar e o
pedido ainda estar sendo processado; nesse caso, espere por uma evidencia
observavel do resultado usando uma assertion ou uma espera orientada a evento.

## 6. Demonstracao executavel

Leia antes de executar:

`src/test/java/br/com/curso/playwright/aula02/LocatorsEAutoWaitDemonstracaoTest.java`

A demonstracao usa HTML local para manter o foco nos conceitos. Identifique:

- onde `data-test` e configurado;
- onde um locator amplo encontra dois elementos;
- como o escopo por regiao elimina a ambiguidade;
- qual assertion aguarda uma mudanca assincrona sem espera fixa.

Execute somente a demonstracao:

```powershell
.\mvn-local.ps1 -Dtest=LocatorsEAutoWaitDemonstracaoTest test
```

## 7. Exercicios no SauceDemo

Implemente:

`src/test/java/br/com/curso/playwright/aula02/LoginNegativoExercicioTest.java`

Remova `@Disabled` quando comecar.

### Exercicio 1 - usuario bloqueado

1. navegue para o SauceDemo;
2. preencha `locked_out_user` e `secret_sauce`;
3. clique em Login usando role e nome acessivel;
4. verifique pelo test ID `error` a mensagem:
   `Epic sadface: Sorry, this user has been locked out.`;
5. confirme que a pagina continua na URL inicial.

### Exercicio 2 - username obrigatorio

1. navegue para o SauceDemo em um teste independente;
2. deixe o username vazio e preencha somente a senha;
3. envie o formulario;
4. verifique pelo test ID `error` a mensagem:
   `Epic sadface: Username is required`.

### Restricoes

- nao use `Thread.sleep`, `waitForTimeout`, XPath ou JavaScript;
- nao use `.first()`/`.nth()` para esconder ambiguidade;
- nao use `isVisible()` dentro de uma assertion JUnit;
- nao crie Page Objects, classe base ou JUnit extension ainda;
- cada teste deve criar e fechar seus proprios recursos nesta aula;
- use `getByRole`, `getByPlaceholder` e `getByTestId` de forma consciente.

## 8. Desafio independente - strictness real

Na pagina de produtos existem varios botoes chamados `Add to cart`. Escreva um
terceiro teste que adicione apenas `Sauce Labs Backpack` ao carrinho.

Este desafio inclui **pesquisa orientada**. Consulte na documentacao oficial a
secao sobre filtragem de locators e investigue `Locator.filter()` e
`Locator.FilterOptions.setHasText()`. Essas APIs nao serao cobradas como
conhecimento previo; o objetivo desta parte e praticar como aprender uma API
nova a partir de uma necessidade concreta.

Requisitos:

- comece pelo conjunto de itens do inventario;
- filtre ou limite o escopo pelo texto `Sauce Labs Backpack`;
- dentro desse item, clique no botao `Add to cart` por role;
- verifique que o contador do carrinho mostra `1`;
- nao use o test ID especifico `add-to-cart-sauce-labs-backpack`, pois o objetivo
  e praticar composicao e escopo de locators.

## Criterios para revisao

- os tres testes passam separadamente e em conjunto;
- nao ha esperas fixas;
- todas as verificacoes de UI usam assertions web-first;
- os locators comunicam role, texto ou contrato de teste;
- o desafio elimina a ambiguidade pelo dominio, nao pela posicao;
- os recursos sao fechados em caso de sucesso ou falha;
- as cinco perguntas da Aula 2 foram respondidas em `docs/PROGRESSO.md`.

## Leitura oficial

- [Locators no Playwright Java](https://playwright.dev/java/docs/locators)
- [Auto-waiting](https://playwright.dev/java/docs/actionability)
- [Assertions](https://playwright.dev/java/docs/test-assertions)

## Correção no formato completo

```powershell
.\course.ps1 exercise 02
.\course.ps1 validate 02
.\course.ps1 solution 02
```

Rubrica: strictness resolvido pelo domínio, prioridade consciente de locator,
assertions com retry, cenários independentes e zero espera fixa. Antes do
gabarito, explique por que sua composição continuaria correta se a posição do
produto mudasse.
