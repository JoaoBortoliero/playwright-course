package br.com.curso.playwright.aula03.support;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;

public final class BrowserFactory {

    private BrowserFactory() {
    }

    public static Browser launch(
            Playwright playwright,
            TestConfig config) {

        BrowserType.LaunchOptions launchOptions =
                new BrowserType.LaunchOptions()
                        .setChannel("msedge")
                        .setHeadless(config.headless());

        return playwright.chromium().launch(launchOptions);
    }
}