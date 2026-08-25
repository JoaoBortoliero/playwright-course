package br.com.curso.playwright.aula08;

import br.com.curso.playwright.aula03.support.BrowserFactory;
import br.com.curso.playwright.aula03.support.TestConfig;
import br.com.curso.playwright.labs.CourseLabExtension;
import br.com.curso.playwright.labs.CourseLabServer;
import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.RequestOptions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(CourseLabExtension.class)
@Tag("demo-08")
class ApiEStorageStateDemonstracao {
  @Test
  void deveValidarGetPostEErroComOwnershipExplicito(CourseLabServer lab) {
    try (Playwright playwright = Playwright.create()) {
      APIRequestContext request = playwright.request().newContext(
          new APIRequest.NewContextOptions().setBaseURL(lab.baseUrl()));
      try {
        APIResponse get = request.get("/api/items");
        try {
          assertEquals(200, get.status());
          assertTrue(get.ok());
          assertTrue(get.headers().get("content-type").contains("application/json"));
          assertTrue(get.text().contains("Backpack"));
        } finally {
          get.dispose();
        }

        APIResponse post = request.post("/api/items",
            RequestOptions.create().setData(Map.of("name", "Novo item")));
        try {
          assertEquals(201, post.status());
          assertTrue(post.text().contains("\"id\":3"));
        } finally {
          post.dispose();
        }

        APIResponse error = request.get("/api/error");
        try {
          assertEquals(503, error.status());
          assertFalse(error.ok());
          assertTrue(error.text().contains("maintenance"));
        } finally {
          error.dispose();
        }
      } finally {
        request.dispose();
      }
    }
  }

  @Test
  void deveCarregarEstadoEmContextoNovoSemAutenticarOAnonimo(CourseLabServer lab)
      throws Exception {
    Path stateFile = Files.createTempFile("playwright-state-", ".json");
    try (Playwright playwright = Playwright.create()) {
      playwright.selectors().setTestIdAttribute("data-test");
      Browser browser = BrowserFactory.launch(playwright, TestConfig.fromSystemProperties());
      try {
        BrowserContext origem = browser.newContext(
            new Browser.NewContextOptions().setBaseURL(lab.baseUrl()));
        Page page = origem.newPage();
        page.navigate("/auth");
        page.getByRole(AriaRole.BUTTON,
            new Page.GetByRoleOptions().setName("Entrar")).click();
        assertThat(page.getByTestId("session")).hasText("Autenticado");
        origem.storageState(new BrowserContext.StorageStateOptions().setPath(stateFile));
        origem.close();

        BrowserContext autenticado = browser.newContext(new Browser.NewContextOptions()
            .setBaseURL(lab.baseUrl()).setStorageStatePath(stateFile));
        try {
          APIResponse profile = autenticado.request().get("/api/profile");
          try {
            assertEquals(200, profile.status());
            assertTrue(profile.text().contains("student"));
          } finally {
            profile.dispose();
          }
        } finally {
          autenticado.close();
        }

        BrowserContext anonimo = browser.newContext(
            new Browser.NewContextOptions().setBaseURL(lab.baseUrl()));
        try {
          APIResponse profile = anonimo.request().get("/api/profile");
          try {
            assertEquals(401, profile.status());
          } finally {
            profile.dispose();
          }
        } finally {
          anonimo.close();
        }
      } finally {
        browser.close();
      }
    } finally {
      Files.deleteIfExists(stateFile);
    }
  }
}
