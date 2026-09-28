package br.com.curso.playwright.aula01;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

class PrimeiroContatoPlaywrightTest {

    private static final String BASE_URL =
            System.getProperty("baseUrl", "https://www.saucedemo.com/");

    @Test
    @DisplayName("deve abrir a pagina inicial do SauceDemo")
    void deveAbrirPaginaInicialDoSauceDemo() {

        try (Playwright playwright = Playwright.create()) {

            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions()
                            .setChannel("msedge")
                            .setHeadless(Boolean.parseBoolean(
                                    System.getProperty("headless", "false")
                            ))
            );
            BrowserContext context = browser.newContext();
            try {
                Page page = context.newPage();
                page.navigate(BASE_URL);
                assertThat(page).hasTitle("Swag Labs");
            } finally {
                context.close();
                browser.close();
            }
        }
    }

}

