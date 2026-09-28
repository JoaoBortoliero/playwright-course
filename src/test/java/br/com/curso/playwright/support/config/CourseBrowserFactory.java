package br.com.curso.playwright.support.config;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;

/** Inicia exclusivamente o Microsoft Edge instalado na maquina. */
public final class CourseBrowserFactory {

    private CourseBrowserFactory() {
    }

    public static Browser launch(
            Playwright playwright,
            CourseConfig config) {

        BrowserType.LaunchOptions options =
                new BrowserType.LaunchOptions()
                        .setChannel("msedge")
                        .setHeadless(config.headless());

        return playwright.chromium().launch(options);
    }
}
