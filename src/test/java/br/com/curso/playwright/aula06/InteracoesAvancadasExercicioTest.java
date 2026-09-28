package br.com.curso.playwright.aula06;

import br.com.curso.playwright.aula03.support.BrowserFactory;
import br.com.curso.playwright.aula03.support.TestConfig;
import br.com.curso.playwright.labs.CourseLabExtension;
import br.com.curso.playwright.labs.CourseLabServer;
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
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.fail;

@Disabled("Aula 6: remova quando os testes de interacao estiverem implementados")
@ExtendWith(CourseLabExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("exercise-06")
class InteracoesAvancadasExercicioTest {

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
    void abrirLaboratorio(CourseLabServer lab) {
        context = browser.newContext();
        context.setDefaultTimeout(config.timeoutMs());
        context.setDefaultNavigationTimeout(config.timeoutMs());
        page = context.newPage();
        page.navigate(lab.baseUrl() + "/interactions");
    }

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

    @AfterAll
    void fecharBrowser() {
        try {
            if (browser != null) {
                browser.close();
            }
        } finally {
            browser = null;

            if (playwright != null) {
                playwright.close();
                playwright = null;
            }
        }
    }

    @Test
    void deveEnviarArquivo() {
        // TODO: Files.createTempFile, setInputFiles, assertion e cleanup.
        fail("Implemente upload");
    }

    @Test
    void deveBaixarEValidarRelatorio() {
        // TODO: waitForDownload antes do clique, saveAs e validacao do conteudo.
        fail("Implemente download");
    }

    @Test
    void deveAceitarDialogo() {
        // TODO: onceDialog, captura da mensagem e accept.
        fail("Implemente dialogo");
    }

    @Test
    void deveCapturarPopup() {
        // TODO: waitForPopup e assertion na Page retornada.
        fail("Implemente popup");
    }

    @Test
    void deveUsarFrameTecladoEAutoRetry() {
        // TODO: FrameLocator, press Enter e conteudo atrasado sem espera fixa.
        fail("Implemente frame, teclado e conteudo atrasado");
    }
}
