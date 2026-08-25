package br.com.curso.playwright.aula04;

import br.com.curso.playwright.aula03.support.BrowserFactory;
import br.com.curso.playwright.aula03.support.TestConfig;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.fail;

@Disabled("Aula 4: remova quando os três checkpoints estiverem implementados")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("exercise-04")
class CatalogoEOrdenacaoExercicioTest {
  private TestConfig config;
  private Playwright playwright;
  private Browser browser;
  private BrowserContext context;
  private Page page;

  // Fixture fornecida: é a aplicação direta da Aula 3, não o objetivo novo da Aula 4.
  @BeforeAll
  void iniciarBrowser() {
    config = TestConfig.fromSystemProperties();
    playwright = Playwright.create();
    playwright.selectors().setTestIdAttribute("data-test");
    PlaywrightAssertions.setDefaultAssertionTimeout(config.timeoutMs());
    browser = BrowserFactory.launch(playwright, config);
  }

  @BeforeEach
  void criarContextoEPagina() {
    context = browser.newContext(
        new Browser.NewContextOptions().setBaseURL(config.baseUrl()));
    context.setDefaultTimeout(config.timeoutMs());
    context.setDefaultNavigationTimeout(config.timeoutMs());
    page = context.newPage();
  }

  @AfterEach
  void fecharContexto() {
    if (context != null) {
      context.close();
      context = null;
      page = null;
    }
  }

  @AfterAll
  void fecharBrowser() {
    if (browser != null) browser.close();
    if (playwright != null) playwright.close();
  }

  @Test
  void deveExibirCatalogoCompleto() {
    // TODO checkpoint B: login, hasCount, allTextContents e comparação por conjunto.
    fail("Implemente o catálogo completo");
  }

  @Test
  void deveOrdenarProdutosPorMenorPreco() {
    // TODO checkpoint C: selectOption, BigDecimal, cópia ordenada e assertEquals.
    fail("Implemente a ordenação monetária");
  }

  @ParameterizedTest(name = "login de {0}")
  @MethodSource("casosDeLogin")
  void deveObservarResultadoDoLogin(
      String usuario, String testIdEsperado, String textoEsperado) {
    // TODO checkpoint D: mesma mecânica, resultado descrito pelos argumentos, sem if.
    fail("Implemente o teste parametrizado");
  }

  static Stream<Arguments> casosDeLogin() {
    return Stream.of(
        Arguments.of("standard_user", "title", "Products"),
        Arguments.of("locked_out_user", "error",
            "Epic sadface: Sorry, this user has been locked out."));
  }

  private void loginAs(String usuario) {
    page.navigate("/");
    page.getByPlaceholder("Username").fill(usuario);
    page.getByPlaceholder("Password").fill("secret_sauce");
    page.getByRole(AriaRole.BUTTON,
        new Page.GetByRoleOptions().setName("Login")).click();
  }
}
