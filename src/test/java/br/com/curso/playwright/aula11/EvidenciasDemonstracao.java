package br.com.curso.playwright.aula11;

import br.com.curso.playwright.support.config.CourseConfig;
import br.com.curso.playwright.support.junit.PlaywrightTest;
import com.microsoft.playwright.Page;
import io.qameta.allure.Allure;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@PlaywrightTest
@Tag("demo-11")
class EvidenciasDemonstracao {
  @Test
  void deveOrganizarRelatorioECriarScreenshotExplicito(Page page, CourseConfig config)
      throws Exception {
    Allure.step("Preparar pedido local", () -> page.setContent("""
        <main><h1>Pedido</h1>
        <button onclick="document.querySelector('p').textContent='Concluído'">Finalizar</button>
        <p data-test="status"></p></main>
        """));
    Allure.step("Finalizar pedido", () -> page.getByText("Finalizar").click());
    Allure.step("Verificar confirmação", () ->
        assertThat(page.getByTestId("status")).hasText("Concluído"));

    Files.createDirectories(config.artifactsDir());
    Path screenshot = config.artifactsDir().resolve("demo-11-estado.png");
    byte[] image = page.screenshot(new Page.ScreenshotOptions()
        .setPath(screenshot).setFullPage(true));
    Allure.addAttachment("Estado final", "image/png",
        new java.io.ByteArrayInputStream(image), ".png");
    Allure.addAttachment("Ambiente", "text/plain",
        "browser=" + config.browser() + ", environment=" + config.environment());

    assertTrue(Files.size(screenshot) > 0);
  }
}
