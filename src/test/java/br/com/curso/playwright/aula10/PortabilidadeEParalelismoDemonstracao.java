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
import org.junit.jupiter.api.Assumptions;
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
  @Tag("cross-browser")
  void deveExecutarSmokeSemFixarMotorNoTeste(CourseLabServer lab) {
    CourseConfig config = CourseConfig.load();
    System.out.printf("thread=%d browser=%s%n",
        Thread.currentThread().getId(), config.browser());

    try (Playwright playwright = Playwright.create()) {
      playwright.selectors().setTestIdAttribute("data-test");
      Browser browser = CourseBrowserFactory.launch(playwright, config);
      BrowserContext context = browser.newContext(
          new Browser.NewContextOptions().setBaseURL(lab.baseUrl()));
      try {
        Page page = context.newPage();
        page.navigate("/");
        assertThat(page.getByRole(AriaRole.HEADING,
            new Page.GetByRoleOptions().setName("Playwright Lab"))).isVisible();

        page.keyboard().press("Tab");
        assertThat(page.getByRole(AriaRole.LINK,
            new Page.GetByRoleOptions().setName("Interações"))).isFocused();
      } finally {
        context.close();
        browser.close();
      }
    }
  }

  @Test
  @Tag("mobile")
  void deveCriarContextoMobileEmChromium(CourseLabServer lab) {
    CourseConfig config = CourseConfig.load();
    Assumptions.assumeTrue(config.browser().equals("chromium"),
        "isMobile não é suportado pelo Firefox; exercício mobile usa Chromium");

    try (Playwright playwright = Playwright.create()) {
      playwright.selectors().setTestIdAttribute("data-test");
      Browser browser = CourseBrowserFactory.launch(playwright, config);
      BrowserContext mobile = browser.newContext(new Browser.NewContextOptions()
          .setBaseURL(lab.baseUrl())
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
        browser.close();
      }
    }
  }
}
