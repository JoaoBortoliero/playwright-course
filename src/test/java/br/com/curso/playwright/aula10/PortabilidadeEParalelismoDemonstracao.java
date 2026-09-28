package br.com.curso.playwright.aula10;

import br.com.curso.playwright.labs.CourseLabExtension;
import br.com.curso.playwright.labs.CourseLabServer;
import br.com.curso.playwright.support.config.CourseBrowserFactory;
import br.com.curso.playwright.support.config.CourseConfig;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(CourseLabExtension.class)
@Tag("demo-10")
class PortabilidadeEParalelismoDemonstracao {

    @Test
    @Tag("smoke")
    void deveExecutarSmokeNoEdgeInstalado(CourseLabServer lab) {
        executarDesktop(lab, true);
    }

    @Test
    @Tag("parallel")
    void deveExecutarSegundoCenarioDesktopIndependente(CourseLabServer lab) {
        executarDesktop(lab, false);
    }

    @Test
    @Tag("mobile")
    void deveCriarContextoMobileNoEdge(CourseLabServer lab) {
        CourseConfig config = CourseConfig.load();
        registrar("mobile");

        try (Playwright playwright = Playwright.create()) {
            playwright.selectors().setTestIdAttribute("data-test");
            Browser browser = CourseBrowserFactory.launch(playwright, config);

            try {
                BrowserContext context = browser.newContext(
                        new Browser.NewContextOptions()
                                .setBaseURL(lab.baseUrl())
                                .setViewportSize(390, 844)
                                .setHasTouch(true)
                                .setIsMobile(true));

                try {
                    Page page = context.newPage();
                    page.navigate("/");

                    assertEquals(390, page.viewportSize().width);
                    assertEquals(844, page.viewportSize().height);
                    assertThat(page.getByTestId("ready")).hasText("ready");
                } finally {
                    context.close();
                }
            } finally {
                browser.close();
            }
        }
    }

    private void executarDesktop(CourseLabServer lab, boolean validarFoco) {
        CourseConfig config = CourseConfig.load();
        registrar(validarFoco ? "smoke" : "desktop-2");

        try (Playwright playwright = Playwright.create()) {
            playwright.selectors().setTestIdAttribute("data-test");
            Browser browser = CourseBrowserFactory.launch(playwright, config);

            try {
                BrowserContext context = browser.newContext(
                        new Browser.NewContextOptions()
                                .setBaseURL(lab.baseUrl()));

                try {
                    Page page = context.newPage();
                    page.navigate("/");

                    assertThat(page.getByRole(
                            AriaRole.HEADING,
                            new Page.GetByRoleOptions().setName("Playwright Lab")))
                            .isVisible();

                    if (validarFoco) {
                        page.keyboard().press("Tab");

                        assertThat(page.getByRole(
                                AriaRole.LINK,
                                new Page.GetByRoleOptions().setName("Interações")))
                                .isFocused();
                    }
                } finally {
                    context.close();
                }
            } finally {
                browser.close();
            }
        }
    }

    private void registrar(String teste) {
        System.out.printf(
                "thread=%d teste=%s%n",
                Thread.currentThread().getId(),
                teste);
    }
}
