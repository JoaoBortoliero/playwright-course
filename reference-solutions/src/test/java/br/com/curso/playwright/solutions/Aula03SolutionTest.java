package br.com.curso.playwright.solutions;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("solution-03")
class Aula03SolutionTest {
  private Playwright playwright;
  private Browser browser;
  private BrowserContext context;
  private Page page;

  @BeforeAll void openBrowser() {
    playwright = Playwright.create();
    playwright.selectors().setTestIdAttribute("data-test");
    browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
        .setHeadless(Boolean.parseBoolean(System.getProperty("headless", "true"))));
  }

  @BeforeEach void newIsolatedPage() {
    context = browser.newContext(new Browser.NewContextOptions()
        .setBaseURL(System.getProperty("baseUrl", "https://www.saucedemo.com/")));
    page = context.newPage();
  }

  @AfterEach void closeContext() { if (context != null) context.close(); }
  @AfterAll void closeBrowser() { if (browser != null) browser.close(); if (playwright != null) playwright.close(); }

  @Test void validLogin() {
    login("standard_user");
    assertThat(page.getByTestId("title")).hasText("Products");
  }

  @Test void lockedUser() {
    login("locked_out_user");
    assertThat(page.getByTestId("error")).containsText("locked out");
  }

  @Test void cleanSession() {
    page.navigate("/");
    assertThat(page.getByTestId("login-button")).isVisible();
  }

  private void login(String user) {
    page.navigate("/");
    page.getByPlaceholder("Username").fill(user);
    page.getByPlaceholder("Password").fill("secret_sauce");
    page.getByTestId("login-button").click();
  }
}
