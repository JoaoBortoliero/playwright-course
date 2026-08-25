package br.com.curso.playwright.aula03;

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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LifecycleJUnitDemonstracaoTest {

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
    if (browser != null) {
      browser.close();
      browser = null;
    }
    if (playwright != null) {
      playwright.close();
      playwright = null;
    }
  }

  @Test
  @DisplayName("deve autenticar em um contexto isolado")
  void deveAutenticarEmContextoIsolado() {
    page.navigate("/");
    page.getByPlaceholder("Username").fill("standard_user");
    page.getByPlaceholder("Password").fill("secret_sauce");
    page.getByRole(
        AriaRole.BUTTON,
        new Page.GetByRoleOptions().setName("Login"))
        .click();

    assertThat(page.getByTestId("title")).hasText("Products");
  }

  @Test
  @DisplayName("deve iniciar outro teste sem sessao autenticada")
  void deveIniciarOutroTesteSemSessaoAutenticada() {
    page.navigate("/");

    assertThat(page.getByRole(
        AriaRole.BUTTON,
        new Page.GetByRoleOptions().setName("Login")))
        .isVisible();
    assertThat(page).hasURL(config.baseUrl());
  }
}
