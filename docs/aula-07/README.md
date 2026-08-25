# Aula 7 — Arquitetura profissional da suíte

Tempo sugerido: 5–6 horas. Pré-requisito: compreender completamente a fixture
manual da Aula 3.

## Objetivos

Separar responsabilidades com Page Objects e componentes, usar a extensão
JUnit do curso e manter os testes como documentação do comportamento.

## 1. O que abstrair

Um Page Object encapsula como uma tela é operada; o teste decide o que deve ser
verdade. Compare:

```java
loginPage.loginAs("standard_user", "secret_sauce");
inventory.addProduct("Sauce Labs Backpack");
assertThat(header.cartBadge()).hasText("1");
```

Evite métodos genéricos como `clickButton(String css)` e Page Objects que
apenas repetem a API do Playwright. A abstração deve falar o domínio. Não crie
uma classe base gigante: composição deixa ownership e dependências explícitos.

Assertions de regra ficam no teste. O Page Object pode expor um `Locator` de
estado ou retornar dados, mas não deve importar JUnit nem decidir o oráculo.

Em Selenium e Playwright, Page Object é padrão de projeto, não recurso do
runner. A diferença prática é que `Locator` pode ser guardado como consulta
reavaliada, enquanto armazenar `WebElement` costuma introduzir staleness.

### Construindo o primeiro Page Object

Um Page Object recebe a `Page` pronta pelo construtor. Ele não abre browser,
contexto nem decide configuração:

```java
public final class LoginPage {
  private final Page page;
  private final Locator username;
  private final Locator password;
  private final Locator loginButton;

  public LoginPage(Page page) {
    this.page = page;
    username = page.getByPlaceholder("Username");
    password = page.getByPlaceholder("Password");
    loginButton = page.getByRole(
        AriaRole.BUTTON,
        new Page.GetByRoleOptions().setName("Login"));
  }

  public void open() {
    page.navigate("/");
  }

  public void loginAs(String user, String secret) {
    username.fill(user);
    password.fill(secret);
    loginButton.click();
  }

  public Locator error() {
    return page.getByTestId("error");
  }
}
```

O teste continua responsável pelo resultado:

```java
LoginPage login = new LoginPage(page);
login.open();
login.loginAs("locked_out_user", "secret_sauce");
assertThat(login.error()).containsText("locked out");
```

Guardar `Locator` é seguro porque ele é uma consulta reavaliada. Guardar texto,
contagem ou `ElementHandle` no construtor criaria uma fotografia possivelmente
obsoleta.

## 2. Componentes e dados

Menu e carrinho aparecem em várias telas; modele `HeaderComponent` uma vez.
Objetos de dados como `Customer` não conhecem Playwright. Uma página pode
compor componentes:

```java
final class InventoryPage {
  private final Page page;
  private final HeaderComponent header;
}
```

Prefira retorno que represente navegação (`CartPage openCart()`) quando ele
melhorar leitura; não construa cadeias fluentes apenas por estética.

### Componente reutilizável

```java
public final class HeaderComponent {
  private final Page page;

  public HeaderComponent(Page page) {
    this.page = page;
  }

  public Locator cartBadge() {
    return page.getByTestId("shopping-cart-badge");
  }

  public void openCart() {
    page.getByTestId("shopping-cart-link").click();
  }
}
```

`HeaderComponent` representa uma região compartilhada, não uma página. O
`InventoryPage` pode recebê-lo ou construí-lo com a mesma `Page`.

### Objeto de dados

```java
public record Customer(String firstName, String lastName, String postalCode) {}
```

Esse record não importa Playwright nem JUnit. Ele pode ser testado e reutilizado
sem browser.

## 3. Extensão JUnit

`@PlaywrightTest` registra `PlaywrightExtension`. Ela cria runtime/browser por
thread e contexto/página por teste, injeta dependências, configura timeouts e
fecha recursos. Não é magia: compare-a com os hooks que você escreveu.

```java
@PlaywrightTest
class CartTest {
  @Test void adicionar(Page page, CourseConfig config) { }
}
```

Em Java, a injeção ocorre pelos parâmetros do método. A extensão aceita:

```java
@Test
void exemplo(
    Page page,
    BrowserContext context,
    CourseConfig config,
    APIRequestContext request) {
}
```

Declare apenas os objetos necessários. Antes de cada teste a extensão cria
contexto e página; depois do teste captura evidências conforme a configuração e
fecha os recursos. Runtime e browser ficam associados à thread, preparando a
arquitetura para o paralelismo da Aula 10.

`CourseConfig` substitui o `TestConfig` didático da Aula 3 e acrescenta URL de
API, ambiente e políticas de evidência. A precedência é propriedade `-D`,
variável de ambiente e valor padrão.

O teste não chama `close()`: a extensão é dona da fixture. Não guarde a `Page`
em campo `static` nem compartilhe Page Objects entre testes.

## 4. Exercício

Execute a demonstração:

```powershell
.\course.ps1 demo 07
```

Leia `ArquiteturaDemonstracao.java`: o teste cria a abstração, executa uma ação
de domínio e mantém a assertion fora dela. Depois implemente, nesta ordem:

- `LoginPage` com `open()` e `loginAs()`;
- `InventoryPage` com produto por nome, ordenação e dados visíveis;
- `HeaderComponent` com badge e navegação ao carrinho;
- `CartPage` e `CheckoutPage` com ações de domínio;
- `ArquiteturaSuiteExercicioTest` usando `@PlaywrightTest`.

Os arquivos iniciais já existem e lançam `UnsupportedOperationException` para
marcar os pontos pendentes. Complete primeiro `LoginPage` e execute apenas o
teste de login. Depois avance por `InventoryPage`, `HeaderComponent`,
`CartPage` e `CheckoutPage`.

Contrato sugerido entre classes:

```text
Teste
 ├─ LoginPage: abrir e autenticar
 ├─ InventoryPage: observar catálogo e adicionar por nome
 ├─ HeaderComponent: badge e abrir carrinho
 ├─ CartPage: itens, remoção e iniciar checkout
 └─ CheckoutPage: identificar comprador, total, finalizar e confirmação
```

Migre um login negativo e uma compra da Aula 5. Não copie locators para o
teste. Não mova assertions para os Page Objects.

### Desafio independente

Desenhe o grafo de dependências e identifique uma abstração que você decidiu
**não** criar. Justifique pelo custo cognitivo.

## Validação e rubrica

```powershell
.\course.ps1 validate 07
```

JavaParser e ArchUnit verificam importações, ciclos e limites de pacotes.

- [ ] Testes narram regras; páginas narram operações.
- [ ] Componentes reutilizados não duplicam locators.
- [ ] Dados não dependem de Playwright/JUnit.
- [ ] Não há assertions JUnit em `pages` ou estado estático de browser.
- [ ] A extensão é dona do fechamento.

Perguntas: qual comportamento não deveria entrar num Page Object? Quando
retornar outra página ajuda? Como a arquitetura reagiria a uma segunda UI?

Erros comuns: `ParameterResolutionException` indica ausência de
`@PlaywrightTest` ou tipo não suportado; `UnsupportedOperationException` mostra
qual método starter ainda não foi implementado; ciclo do ArchUnit indica que
páginas/componentes passaram a depender de testes ou umas das outras sem uma
direção clara.

Leituras: [Page Objects](https://playwright.dev/java/docs/pom),
[fixtures JUnit](https://playwright.dev/java/docs/test-runners) e
[composition over inheritance](https://en.wikipedia.org/wiki/Composition_over_inheritance).

Solução após a autoavaliação: `.\course.ps1 solution 07`.
