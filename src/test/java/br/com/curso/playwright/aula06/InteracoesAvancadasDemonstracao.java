package br.com.curso.playwright.aula06;

import br.com.curso.playwright.aula03.support.BrowserFactory;
import br.com.curso.playwright.aula03.support.TestConfig;
import br.com.curso.playwright.labs.CourseLabExtension;
import br.com.curso.playwright.labs.CourseLabServer;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Download;
import com.microsoft.playwright.FrameLocator;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(CourseLabExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("demo-06")
class InteracoesAvancadasDemonstracao {
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

  @Test
  void deveFazerUploadEDownload() throws Exception {
    Path upload = Files.createTempFile("curso-playwright-", ".txt");
    Path destino = Files.createTempFile("curso-download-", ".txt");
    try {
      Files.writeString(upload, "conteúdo de teste");
      page.getByLabel("Arquivo").setInputFiles(upload);
      assertThat(page.getByTestId("file-name")).hasText(upload.getFileName().toString());

      Download download = page.waitForDownload(() -> page.getByRole(AriaRole.LINK,
          new Page.GetByRoleOptions().setName("Baixar relatório")).click());
      assertEquals("report.txt", download.suggestedFilename());
      download.saveAs(destino);
      assertEquals("relatório determinístico\n", Files.readString(destino));
    } finally {
      Files.deleteIfExists(upload);
      Files.deleteIfExists(destino);
    }
  }

  @Test
  void deveObservarDialogoEPopupAntesDosCliques() {
    AtomicReference<String> mensagem = new AtomicReference<>();
    page.onceDialog(dialog -> {
      mensagem.set(dialog.message());
      dialog.accept();
    });
    page.getByRole(AriaRole.BUTTON,
        new Page.GetByRoleOptions().setName("Abrir diálogo")).click();
    assertEquals("Confirmação do laboratório", mensagem.get());

    Page popup = page.waitForPopup(() -> page.getByRole(AriaRole.BUTTON,
        new Page.GetByRoleOptions().setName("Abrir popup")).click());
    assertThat(popup.getByTestId("popup-status")).hasText("Popup carregado");
  }

  @Test
  void deveUsarEscopoDoFrameTecladoEAssertionComRetry() {
    FrameLocator frame = page.frameLocator("iframe[title='Área incorporada']");
    Locator confirmar = frame.getByRole(AriaRole.BUTTON,
        new FrameLocator.GetByRoleOptions().setName("Confirmar"));
    confirmar.click();
    assertThat(frame.getByRole(AriaRole.BUTTON,
        new FrameLocator.GetByRoleOptions().setName("Concluído"))).isVisible();

    page.getByLabel("Atalho").press("Enter");
    assertThat(page.getByTestId("key")).hasText("Enter recebido");

    page.getByRole(AriaRole.BUTTON,
        new Page.GetByRoleOptions().setName("Carregar")).click();
    assertThat(page.getByTestId("delayed")).hasText("Conteúdo pronto");
  }
}
