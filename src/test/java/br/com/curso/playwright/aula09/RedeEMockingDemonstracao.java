package br.com.curso.playwright.aula09;

import br.com.curso.playwright.labs.CourseLabExtension;
import br.com.curso.playwright.labs.CourseLabServer;
import br.com.curso.playwright.support.junit.PlaywrightTest;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@PlaywrightTest
@ExtendWith(CourseLabExtension.class)
@Tag("demo-09")
class RedeEMockingDemonstracao {
  @Test
  void deveSincronizarComRespostaEValidarUi(Page page, CourseLabServer lab) {
    List<String> requests = new CopyOnWriteArrayList<>();
    page.onRequest(request -> requests.add(request.method() + " " + request.url()));
    page.navigate(lab.baseUrl() + "/network");

    Response response = page.waitForResponse(
        candidate -> candidate.url().endsWith("/api/items")
            && candidate.request().method().equals("GET"),
        () -> page.getByRole(AriaRole.BUTTON,
            new Page.GetByRoleOptions().setName("Carregar itens")).click());

    assertEquals(200, response.status());
    assertThat(page.getByTestId("result")).containsText("Backpack");
    assertTrue(requests.stream().anyMatch(value -> value.endsWith("/api/items")));
  }

  @Test
  void deveSimularContratoVazioSemAtingirAServerApi(Page page, CourseLabServer lab) {
    page.route("**/api/items", route -> route.fulfill(new Route.FulfillOptions()
        .setStatus(200).setContentType("application/json").setBody("[]")));
    page.navigate(lab.baseUrl() + "/network");
    page.getByRole(AriaRole.BUTTON,
        new Page.GetByRoleOptions().setName("Carregar itens")).click();
    assertThat(page.getByTestId("result")).hasText("items:[]");
  }

  @Test
  void deveTratarFalhaDeTransporteEColetarPageError(Page page, CourseLabServer lab) {
    page.route("**/api/blocked", route -> route.abort());
    page.navigate(lab.baseUrl() + "/network");
    page.getByRole(AriaRole.BUTTON,
        new Page.GetByRoleOptions().setName("Requisição bloqueável")).click();
    assertThat(page.getByTestId("result")).hasText("blocked:erro");

    List<String> errors = new CopyOnWriteArrayList<>();
    page.onPageError(errors::add);
    page.setContent("""
        <button onclick="setTimeout(() => {
          document.querySelector('p').textContent='executado';
          throw new Error('falha controlada');
        }, 0)">Executar</button><p data-test="state"></p>
        """);
    page.getByText("Executar").click();
    assertThat(page.getByTestId("state")).hasText("executado");
    assertTrue(errors.stream().anyMatch(value -> value.contains("falha controlada")));
  }
}
