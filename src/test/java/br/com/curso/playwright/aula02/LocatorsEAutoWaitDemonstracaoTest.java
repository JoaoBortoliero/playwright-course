package br.com.curso.playwright.aula02;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class LocatorsEAutoWaitDemonstracaoTest {

  @Test
  @DisplayName("deve resolver ambiguidade pelo dominio e aguardar o resultado")
  void deveResolverAmbiguidadeEAguardarResultado() {
    try (Playwright playwright = Playwright.create()) {
      playwright.selectors().setTestIdAttribute("data-test");
      Browser browser = playwright.chromium().launch();
      BrowserContext context = browser.newContext();

      try {
        Page page = context.newPage();
        page.setContent("""
            <section aria-label="Cadastro">
              <button onclick="salvarCadastro()">Salvar</button>
              <p data-test="status-cadastro" hidden></p>
            </section>
            <section aria-label="Preferencias">
              <button>Salvar</button>
            </section>
            <script>
              function salvarCadastro() {
                setTimeout(() => {
                  const status = document.querySelector('[data-test=status-cadastro]');
                  status.textContent = 'Cadastro salvo';
                  status.hidden = false;
                }, 300);
              }
            </script>
            """);

        Locator todosOsBotoesSalvar = page.getByRole(
            AriaRole.BUTTON,
            new Page.GetByRoleOptions().setName("Salvar"));
        assertEquals(2, todosOsBotoesSalvar.count());

        Locator cadastro = page.getByRole(
            AriaRole.REGION,
            new Page.GetByRoleOptions().setName("Cadastro"));
        cadastro.getByRole(
            AriaRole.BUTTON,
            new Locator.GetByRoleOptions().setName("Salvar"))
            .click();

        assertThat(page.getByTestId("status-cadastro"))
            .hasText("Cadastro salvo");
        assertThat(page.getByTestId("status-cadastro"))
            .isVisible();
      } finally {
        context.close();
        browser.close();
      }
    }
  }
}

