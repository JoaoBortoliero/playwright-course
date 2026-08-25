package br.com.curso.playwright.aula05;

import br.com.curso.playwright.aula03.support.BrowserFactory;
import br.com.curso.playwright.aula03.support.TestConfig;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
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

@Disabled("Aula 5: remova quando a jornada e os negativos estiverem completos")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("exercise-05")
class CheckoutExercicioTest {
  record Customer(String firstName, String lastName, String postalCode) {}

  private TestConfig config;
  private Playwright playwright;
  private Browser browser;
  private BrowserContext context;
  private Page page;

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
    page = context.newPage();
  }

  @AfterEach
  void fecharContexto() {
    if (context != null) context.close();
  }

  @AfterAll
  void fecharBrowser() {
    if (browser != null) browser.close();
    if (playwright != null) playwright.close();
  }

  @Test
  void deveConcluirCompraEValidarValores() {
    // TODO: checkpoints de produto, badge, carrinho, checkout, BigDecimal e confirmação.
    fail("Implemente a jornada positiva da Aula 5");
  }

  @ParameterizedTest(name = "dados {0}/{1}/{2} produzem {3}")
  @MethodSource("camposObrigatorios")
  void deveValidarCamposObrigatorios(
      String firstName, String lastName, String postalCode, String mensagem) {
    // TODO: prepare carrinho, preencha os dados, continue e valide o erro.
    fail("Implemente os negativos parametrizados da Aula 5");
  }

  static Stream<Arguments> camposObrigatorios() {
    return Stream.of(
        Arguments.of("", "Lovelace", "01000-000", "First Name is required"),
        Arguments.of("Ada", "", "01000-000", "Last Name is required"),
        Arguments.of("Ada", "Lovelace", "", "Postal Code is required"));
  }
}
