package br.com.curso.playwright.aula05;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("demo-05")
class CheckoutEDinheiroDemonstracao {
  @Test
  void deveLocalizarProdutoPeloDominioEObservarATransicao() {
    try (Playwright playwright = Playwright.create()) {
      playwright.selectors().setTestIdAttribute("data-test");
      Browser browser = playwright.chromium().launch(
              new BrowserType.LaunchOptions()
                      .setChannel("msedge")
                      .setHeadless(false)
      );
      BrowserContext context = browser.newContext();
      try {
        Page page = context.newPage();
        page.setContent("""
            <article data-test="item"><h2>Backpack</h2><button>Adicionar</button></article>
            <article data-test="item"><h2>Bike Light</h2><button>Adicionar</button></article>
            <span data-test="badge">0</span>
            <script>
              document.querySelectorAll('button').forEach(button =>
                button.onclick = () => {
                  const badge = document.querySelector('[data-test=badge]');
                  badge.textContent = String(Number(badge.textContent) + 1);
                });
            </script>
            """);

        Locator bikeLight = page.getByTestId("item")
            .filter(new Locator.FilterOptions().setHasText("Bike Light"));
        bikeLight.getByRole(AriaRole.BUTTON,
            new Locator.GetByRoleOptions().setName("Adicionar")).click();

        assertThat(page.getByTestId("badge")).hasText("1");
      } finally {
        context.close();
        browser.close();
      }
    }
  }

  @Test
  void deveCalcularTotalSemPontoFlutuante() {
    BigDecimal subtotal = dinheiro("Item total: $39.98");
    BigDecimal imposto = dinheiro("Tax: $3.20");
    BigDecimal total = dinheiro("Total: $43.18");

    assertEquals(0, subtotal.add(imposto).compareTo(total));
  }

  private static BigDecimal dinheiro(String rotulo) {
    return new BigDecimal(rotulo.replaceAll("[^0-9.]", ""));
  }
}
