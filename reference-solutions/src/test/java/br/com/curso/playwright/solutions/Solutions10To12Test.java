package br.com.curso.playwright.solutions;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Tracing;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Allure;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.io.ByteArrayInputStream;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Solutions10To12Test {
  @Test @Tag("solution-10") @Tag("smoke") @Tag("cross-browser")
  void aula10SmokePortavelETeclado() throws Exception {
    try (ReferenceLab lab = ReferenceLab.start();
         ReferenceSession session = new ReferenceSession(lab.url())) {
      session.page.navigate("/");
      assertThat(session.page.getByRole(AriaRole.HEADING,
          new Page.GetByRoleOptions().setName("Playwright Lab"))).isVisible();
      session.page.keyboard().press("Tab");
      assertThat(session.page.getByRole(AriaRole.LINK,
          new Page.GetByRoleOptions().setName("Interações"))).isFocused();
    }
  }

  @Test @Tag("solution-10") @Tag("mobile")
  void aula10ContextoMobileIsolado() throws Exception {
    Assumptions.assumeTrue(System.getProperty("browser", "chromium").equals("chromium"),
        "emulação isMobile é demonstrada em Chromium");
    try (ReferenceLab lab = ReferenceLab.start(); ReferenceSession session = new ReferenceSession(lab.url())) {
      BrowserContext mobile = session.browser.newContext(new Browser.NewContextOptions()
          .setBaseURL(lab.url()).setViewportSize(390, 844).setHasTouch(true).setIsMobile(true));
      try {
        Page page = mobile.newPage();
        page.navigate("/");
        assertThat(page.getByRole(AriaRole.HEADING)).hasText("Playwright Lab");
        assertTrue(page.viewportSize().width == 390);
      } finally { mobile.close(); }
    }
  }

  @Test @Tag("solution-11")
  void aula11TraceEScreenshotComPolitica() throws Exception {
    try (ReferenceLab lab = ReferenceLab.start(); ReferenceSession session = new ReferenceSession(lab.url())) {
      Path directory = Files.createTempDirectory("playwright-evidence-");
      Allure.step("Iniciar trace com DOM, fonte e screenshots", () ->
          session.context.tracing().start(new Tracing.StartOptions()
              .setScreenshots(true).setSnapshots(true).setSources(true)));
      Allure.step("Abrir laboratório e verificar heading", () -> {
        session.page.navigate("/");
        assertThat(session.page.getByRole(AriaRole.HEADING)).hasText("Playwright Lab");
      });
      byte[] image = session.page.screenshot(
          new Page.ScreenshotOptions().setPath(directory.resolve("state.png")));
      Allure.addAttachment("Estado", "image/png", new ByteArrayInputStream(image), ".png");
      session.context.tracing().stop(new Tracing.StopOptions().setPath(directory.resolve("trace.zip")));
      assertTrue(Files.size(directory.resolve("state.png")) > 0);
      assertTrue(Files.size(directory.resolve("trace.zip")) > 0);
    }
  }

  @Test @Tag("solution-12") @Tag("smoke") @Tag("regression")
  void aula12CapstoneCompraComArquitetura() {
    try (ReferenceSession session = new ReferenceSession()) {
      Login login = new Login(session.page);
      login.openAndLogin("standard_user", "secret_sauce");
      Inventory inventory = new Inventory(session.page);
      assertThat(inventory.title()).hasText("Products");
      inventory.add("Sauce Labs Backpack");
      inventory.openCart();
      Cart cart = new Cart(session.page);
      assertThat(cart.items()).hasCount(1);
      cart.checkout();
      Checkout checkout = new Checkout(session.page);
      checkout.identify("Grace", "Hopper", "10001");
      assertThat(checkout.total()).containsText("Total: $");
      checkout.finish();
      assertThat(checkout.confirmation()).hasText("Thank you for your order!");
    }
  }

  @Test @Tag("solution-12") @Tag("negative") @Tag("regression")
  void aula12CapstoneMantemNegativosLegiveis() {
    try (ReferenceSession session = new ReferenceSession()) {
      Login login = new Login(session.page);
      login.openAndLogin("locked_out_user", "secret_sauce");
      assertThat(session.page.getByTestId("error")).containsText("locked out");
    }

    try (ReferenceSession session = new ReferenceSession()) {
      Login login = new Login(session.page);
      login.openAndLogin("standard_user", "secret_sauce");
      Inventory inventory = new Inventory(session.page);
      inventory.add("Sauce Labs Backpack");
      inventory.openCart();
      Cart cart = new Cart(session.page);
      cart.checkout();
      session.page.getByTestId("continue").click();
      assertThat(session.page.getByTestId("error")).containsText("First Name is required");
    }
  }

  private record Login(Page page) {
    void openAndLogin(String user, String password) {
      page.navigate("/"); page.getByPlaceholder("Username").fill(user);
      page.getByPlaceholder("Password").fill(password); page.getByTestId("login-button").click();
    }
  }
  private record Inventory(Page page) {
    com.microsoft.playwright.Locator title() { return page.getByTestId("title"); }
    void add(String name) {
      page.getByTestId("inventory-item").filter(
          new com.microsoft.playwright.Locator.FilterOptions().setHasText(name))
          .getByRole(AriaRole.BUTTON,
              new com.microsoft.playwright.Locator.GetByRoleOptions().setName("Add to cart")).click();
    }
    void openCart() { page.getByTestId("shopping-cart-link").click(); }
  }
  private record Cart(Page page) {
    com.microsoft.playwright.Locator items() { return page.getByTestId("inventory-item"); }
    void checkout() { page.getByTestId("checkout").click(); }
  }
  private record Checkout(Page page) {
    void identify(String first, String last, String postal) {
      page.getByTestId("firstName").fill(first); page.getByTestId("lastName").fill(last);
      page.getByTestId("postalCode").fill(postal); page.getByTestId("continue").click();
    }
    com.microsoft.playwright.Locator total() { return page.getByTestId("total-label"); }
    void finish() { page.getByTestId("finish").click(); }
    com.microsoft.playwright.Locator confirmation() { return page.getByTestId("complete-header"); }
  }
}
