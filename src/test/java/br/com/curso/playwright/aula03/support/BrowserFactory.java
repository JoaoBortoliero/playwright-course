package br.com.curso.playwright.aula03.support;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;

public final class BrowserFactory {

    private BrowserFactory() {
    }

    public static Browser launch(Playwright playwright, TestConfig config) {
        BrowserType browserType = switch (config.browser()) {
            case "chromium" -> playwright.chromium();
            case "firefox" -> playwright.firefox();
            case "webkit" -> playwright.webkit();
            default -> throw new IllegalStateException(
                    "TestConfig aceitou um browser desconhecido: " + config.browser());
        };

        BrowserType.LaunchOptions launchOptions =
                new BrowserType.LaunchOptions()
                        .setHeadless(config.headless());
        if ("chromium".equals(config.browser())) {
            launchOptions.setChannel("msedge");
        }

        return browserType.launch(launchOptions);
    }
}
