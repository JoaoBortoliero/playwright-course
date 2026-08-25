package br.com.curso.playwright.solutions;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

final class ReferenceSession implements AutoCloseable {
  final Playwright playwright;
  final Browser browser;
  final BrowserContext context;
  final Page page;

  ReferenceSession() { this(System.getProperty("baseUrl", "https://www.saucedemo.com/")); }

  ReferenceSession(String baseUrl) {
    playwright = Playwright.create();
    playwright.selectors().setTestIdAttribute("data-test");
    String browserName = System.getProperty("browser", "chromium");
    BrowserType type = switch (browserName) {
      case "firefox" -> playwright.firefox();
      case "webkit" -> playwright.webkit();
      default -> playwright.chromium();
    };
    browser = type.launch(new BrowserType.LaunchOptions().setHeadless(
        Boolean.parseBoolean(System.getProperty("headless", "true"))));
    context = browser.newContext(new Browser.NewContextOptions().setBaseURL(baseUrl));
    context.setDefaultTimeout(10_000);
    page = context.newPage();
  }

  void login(String username) {
    page.navigate("/");
    page.getByPlaceholder("Username").fill(username);
    page.getByPlaceholder("Password").fill("secret_sauce");
    page.getByTestId("login-button").click();
  }

  @Override public void close() {
    context.close();
    browser.close();
    playwright.close();
  }
}
