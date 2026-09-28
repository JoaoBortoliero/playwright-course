package br.com.curso.playwright.solutions;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Tracing;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Allure;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Solutions10To12Test {

    @Test
    @Tag("solution-10")
    @Tag("smoke")
    void aula10SmokeNoEdgeETeclado() throws Exception {
        try (ReferenceLab lab = ReferenceLab.start();
             ReferenceSession session = new ReferenceSession(lab.url())) {

            session.page.navigate("/");
            assertThat(session.page.getByRole(
                    AriaRole.HEADING,
                    new Page.GetByRoleOptions().setName("Playwright Lab")))
                    .isVisible();
            session.page.keyboard().press("Tab");
            assertThat(session.page.getByRole(
                    AriaRole.LINK,
                    new Page.GetByRoleOptions().setName("Interacoes")))
                    .isFocused();
        }
    }

    @Test
    @Tag("solution-10")
    @Tag("parallel")
    void aula10SegundoCenarioDesktopIndependente() throws Exception {
        try (ReferenceLab lab = ReferenceLab.start();
             ReferenceSession session = new ReferenceSession(lab.url())) {

            System.out.printf("thread=%d teste=desktop-2%n",
                    Thread.currentThread().getId());
            session.page.navigate("/");
            assertThat(session.page.getByTestId("ready")).hasText("ready");
        }
    }

    @Test
    @Tag("solution-10")
    @Tag("mobile")
    void aula10ContextoMobileNoEdge() throws Exception {
        try (ReferenceLab lab = ReferenceLab.start();
             ReferenceSession session = new ReferenceSession(lab.url())) {

            BrowserContext mobile = session.browser.newContext(
                    new Browser.NewContextOptions()
                            .setBaseURL(lab.url())
                            .setViewportSize(390, 844)
                            .setHasTouch(true)
                            .setIsMobile(true));
            try {
                Page page = mobile.newPage();
                page.navigate("/");
                assertEquals(390, page.viewportSize().width);
                assertEquals(844, page.viewportSize().height);
                assertThat(page.getByTestId("ready")).hasText("ready");
            } finally {
                mobile.close();
            }
        }
    }

    @Test
    @Tag("solution-11")
    void aula11TraceEScreenshotComPolitica() throws Exception {
        try (ReferenceLab lab = ReferenceLab.start();
             ReferenceSession session = new ReferenceSession(lab.url())) {

            Path directory = Files.createTempDirectory("playwright-evidence-");
            Path screenshot = directory.resolve("state.png");
            Path trace = directory.resolve("trace.zip");

            try {
                Allure.step("Iniciar trace", () ->
                        session.context.tracing().start(
                                new Tracing.StartOptions()
                                        .setScreenshots(true)
                                        .setSnapshots(true)
                                        .setSources(true)));

                Allure.step("Abrir laboratorio e verificar heading", () -> {
                    session.page.navigate("/");
                    assertThat(session.page.getByRole(AriaRole.HEADING))
                            .hasText("Playwright Lab");
                });

                byte[] image = session.page.screenshot(
                        new Page.ScreenshotOptions().setPath(screenshot));
                Allure.addAttachment(
                        "Estado",
                        "image/png",
                        new ByteArrayInputStream(image),
                        ".png");

                session.context.tracing().stop(
                        new Tracing.StopOptions().setPath(trace));

                assertTrue(Files.size(screenshot) > 0);
                assertTrue(Files.size(trace) > 0);
            } finally {
                Files.deleteIfExists(screenshot);
                Files.deleteIfExists(trace);
                Files.deleteIfExists(directory);
            }
        }
    }

    @Test
    @Tag("solution-12")
    @Tag("smoke")
    @Tag("regression")
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

    @Test
    @Tag("solution-12")
    @Tag("negative")
    @Tag("regression")
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
            assertThat(session.page.getByTestId("error"))
                    .containsText("First Name is required");
        }
    }

    private record Login(Page page) {
        void openAndLogin(String user, String password) {
            page.navigate("/");
            page.getByPlaceholder("Username").fill(user);
            page.getByPlaceholder("Password").fill(password);
            page.getByTestId("login-button").click();
        }
    }

    private record Inventory(Page page) {
        com.microsoft.playwright.Locator title() {
            return page.getByTestId("title");
        }

        void add(String name) {
            page.getByTestId("inventory-item")
                    .filter(new com.microsoft.playwright.Locator.FilterOptions()
                            .setHasText(name))
                    .getByRole(
                            AriaRole.BUTTON,
                            new com.microsoft.playwright.Locator.GetByRoleOptions()
                                    .setName("Add to cart"))
                    .click();
        }

        void openCart() {
            page.getByTestId("shopping-cart-link").click();
        }
    }

    private record Cart(Page page) {
        com.microsoft.playwright.Locator items() {
            return page.getByTestId("inventory-item");
        }

        void checkout() {
            page.getByTestId("checkout").click();
        }
    }

    private record Checkout(Page page) {
        void identify(String first, String last, String postal) {
            page.getByTestId("firstName").fill(first);
            page.getByTestId("lastName").fill(last);
            page.getByTestId("postalCode").fill(postal);
            page.getByTestId("continue").click();
        }

        com.microsoft.playwright.Locator total() {
            return page.getByTestId("total-label");
        }

        void finish() {
            page.getByTestId("finish").click();
        }

        com.microsoft.playwright.Locator confirmation() {
            return page.getByTestId("complete-header");
        }
    }
}
