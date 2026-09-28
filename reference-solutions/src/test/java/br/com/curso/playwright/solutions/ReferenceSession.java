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

    ReferenceSession() {
        this(System.getProperty("baseUrl", "https://www.saucedemo.com/"));
    }

    ReferenceSession(String baseUrl) {
        Playwright createdPlaywright = Playwright.create();
        Browser createdBrowser = null;
        BrowserContext createdContext = null;

        try {
            createdPlaywright.selectors().setTestIdAttribute("data-test");
            createdBrowser = createdPlaywright.chromium().launch(
                    new BrowserType.LaunchOptions()
                            .setChannel("msedge")
                            .setHeadless(Boolean.parseBoolean(
                                    System.getProperty("headless", "false"))));
            createdContext = createdBrowser.newContext(
                    new Browser.NewContextOptions().setBaseURL(baseUrl));
            createdContext.setDefaultTimeout(10_000);

            playwright = createdPlaywright;
            browser = createdBrowser;
            context = createdContext;
            page = createdContext.newPage();
        } catch (RuntimeException error) {
            if (createdContext != null) {
                try {
                    createdContext.close();
                } catch (RuntimeException closeError) {
                    error.addSuppressed(closeError);
                }
            }
            if (createdBrowser != null) {
                try {
                    createdBrowser.close();
                } catch (RuntimeException closeError) {
                    error.addSuppressed(closeError);
                }
            }
            try {
                createdPlaywright.close();
            } catch (RuntimeException closeError) {
                error.addSuppressed(closeError);
            }
            throw error;
        }
    }

    void login(String username) {
        page.navigate("/");
        page.getByPlaceholder("Username").fill(username);
        page.getByPlaceholder("Password").fill("secret_sauce");
        page.getByTestId("login-button").click();
    }

    @Override
    public void close() {
        RuntimeException failure = null;
        try {
            context.close();
        } catch (RuntimeException error) {
            failure = error;
        }
        try {
            browser.close();
        } catch (RuntimeException error) {
            if (failure == null) failure = error;
            else failure.addSuppressed(error);
        }
        try {
            playwright.close();
        } catch (RuntimeException error) {
            if (failure == null) failure = error;
            else failure.addSuppressed(error);
        }
        if (failure != null) throw failure;
    }
}
