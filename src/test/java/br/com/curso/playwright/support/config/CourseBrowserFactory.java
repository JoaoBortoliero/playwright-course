package br.com.curso.playwright.support.config;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;

public final class CourseBrowserFactory {

    private CourseBrowserFactory() {
    }

    public static Browser launch(
            Playwright playwright,
            CourseConfig config
    ) {
        BrowserType type = switch (config.browser()) {
            case "chromium" -> playwright.chromium();
            case "firefox" -> playwright.firefox();
            case "webkit" -> playwright.webkit();
            default -> throw new IllegalStateException(
                    "Browser já deveria ter sido validado: " + config.browser()
            );
        };

        BrowserType.LaunchOptions options =
                new BrowserType.LaunchOptions()
                        .setHeadless(config.headless());

        if ("chromium".equals(config.browser())) {
            options.setChannel("msedge");
        }

        return type.launch(options);
    }
}