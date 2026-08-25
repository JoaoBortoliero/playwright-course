package br.com.curso.playwright.aula06;

import br.com.curso.playwright.aula03.support.BrowserFactory;
import br.com.curso.playwright.aula03.support.TestConfig;
import br.com.curso.playwright.labs.CourseLabExtension;
import br.com.curso.playwright.labs.CourseLabServer;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.fail;

@Disabled("Aula 6: remova quando os testes de interação estiverem implementados")
@ExtendWith(CourseLabExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("exercise-06")
class InteracoesAvancadasExercicioTest {
  private Playwright playwright;
  private Browser browser;
  private BrowserContext context;
  private Page page;

  @BeforeAll
  void iniciarBrowser() {
    TestConfig config = TestConfig.fromSystemProperties();
    playwright = Playwright.create();
    playwright.selectors().setTestIdAttribute("data-test");
    browser = BrowserFactory.launch(playwright, config);
  }

  @BeforeEach
  void abrirLaboratorio(CourseLabServer lab) {
    context = browser.newContext();
    page = context.newPage();
    page.navigate(lab.baseUrl() + "/interactions");
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

  @Test void deveEnviarArquivo() {
    // TODO: Files.createTempFile + setInputFiles + assertion + cleanup.
    fail("Implemente upload");
  }

  @Test void deveBaixarEValidarRelatorio() {
    // TODO: waitForDownload antes do clique, saveAs e conteúdo.
    fail("Implemente download");
  }

  @Test void deveAceitarDialogo() {
    // TODO: onceDialog, mensagem e accept.
    fail("Implemente diálogo");
  }

  @Test void deveCapturarPopup() {
    // TODO: waitForPopup e assertion na Page retornada.
    fail("Implemente popup");
  }

  @Test void deveUsarFrameTecladoEAutoRetry() {
    // TODO: frameLocator, press Enter e conteúdo delayed sem sleep.
    fail("Implemente frame, teclado e conteúdo atrasado");
  }
}
