package br.com.curso.playwright.solutions;

import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Download;
import com.microsoft.playwright.FrameLocator;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.RequestOptions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Solutions06To09Test {
  @Test
  @Tag("solution-06")
  void aula06ArquivosEventosFrameETeclado() throws Exception {
    try (ReferenceLab lab = ReferenceLab.start();
         ReferenceSession session = new ReferenceSession(lab.url())) {
      session.page.navigate("/interactions");

      Path upload = Files.createTempFile("playwright-course-", ".txt");
      Path saved = Files.createTempFile("playwright-download-", ".txt");
      try {
        Files.writeString(upload, "arquivo de estudo");
        session.page.getByLabel("Arquivo").setInputFiles(upload);
        assertThat(session.page.getByTestId("file-name"))
            .hasText(upload.getFileName().toString());

        Download download = session.page.waitForDownload(() ->
            session.page.getByRole(AriaRole.LINK,
                new Page.GetByRoleOptions().setName("Baixar relatório")).click());
        assertEquals("report.txt", download.suggestedFilename());
        download.saveAs(saved);
        assertEquals("relatório determinístico\n", Files.readString(saved));
      } finally {
        Files.deleteIfExists(upload);
        Files.deleteIfExists(saved);
      }

      AtomicReference<String> dialogText = new AtomicReference<>();
      session.page.onceDialog(dialog -> {
        dialogText.set(dialog.message());
        dialog.accept();
      });
      session.page.getByRole(AriaRole.BUTTON,
          new Page.GetByRoleOptions().setName("Abrir diálogo")).click();
      assertEquals("Confirmação do laboratório", dialogText.get());

      Page popup = session.page.waitForPopup(() ->
          session.page.getByRole(AriaRole.BUTTON,
              new Page.GetByRoleOptions().setName("Abrir popup")).click());
      assertThat(popup.getByTestId("popup-status")).hasText("Popup carregado");

      FrameLocator frame = session.page.frameLocator("iframe[title='Área incorporada']");
      frame.getByRole(AriaRole.BUTTON,
          new FrameLocator.GetByRoleOptions().setName("Confirmar")).click();
      assertThat(frame.getByRole(AriaRole.BUTTON,
          new FrameLocator.GetByRoleOptions().setName("Concluído"))).isVisible();

      session.page.getByLabel("Atalho").press("Enter");
      assertThat(session.page.getByTestId("key")).hasText("Enter recebido");
      session.page.getByRole(AriaRole.BUTTON,
          new Page.GetByRoleOptions().setName("Carregar")).click();
      assertThat(session.page.getByTestId("delayed")).hasText("Conteúdo pronto");
    }
  }

  @Test
  @Tag("solution-07")
  void aula07PageObjectsMantemAssertionsNoTeste() {
    try (ReferenceSession session = new ReferenceSession()) {
      LoginPage login = new LoginPage(session.page);
      login.open();
      login.loginAs("standard_user", "secret_sauce");

      InventoryPage inventory = new InventoryPage(session.page);
      assertThat(inventory.title()).hasText("Products");
      inventory.add("Sauce Labs Backpack");
      assertThat(inventory.cartBadge()).hasText("1");
    }
  }

  @Test
  @Tag("solution-08")
  void aula08GetPostErroEStorageStateIsolado() throws Exception {
    try (ReferenceLab lab = ReferenceLab.start(); Playwright playwright = Playwright.create()) {
      playwright.selectors().setTestIdAttribute("data-test");
      APIRequestContext request = playwright.request().newContext(
          new APIRequest.NewContextOptions().setBaseURL(lab.url()));
      try {
        APIResponse get = request.get("/api/items");
        try {
          assertEquals(200, get.status());
          assertTrue(get.text().contains("Backpack"));
          assertTrue(get.text().contains("Bike Light"));
        } finally { get.dispose(); }

        APIResponse post = request.post("/api/items",
            RequestOptions.create().setData(Map.of("name", "Novo item")));
        try {
          assertEquals(201, post.status());
          assertTrue(post.text().contains("\"id\":3"));
        } finally { post.dispose(); }

        APIResponse error = request.get("/api/error");
        try {
          assertEquals(503, error.status());
          assertFalse(error.ok());
        } finally { error.dispose(); }
      } finally {
        request.dispose();
      }

      Browser browser = playwright.chromium().launch();
      Path state = Files.createTempFile("playwright-state-", ".json");
      try {
        BrowserContext origem = browser.newContext(
            new Browser.NewContextOptions().setBaseURL(lab.url()));
        Page page = origem.newPage();
        page.navigate("/auth");
        page.getByText("Entrar").click();
        assertThat(page.getByTestId("session")).hasText("Autenticado");
        origem.storageState(new BrowserContext.StorageStateOptions().setPath(state));
        origem.close();

        BrowserContext autenticado = browser.newContext(new Browser.NewContextOptions()
            .setBaseURL(lab.url()).setStorageStatePath(state));
        try {
          APIResponse profile = autenticado.request().get("/api/profile");
          try { assertEquals(200, profile.status()); }
          finally { profile.dispose(); }
        } finally { autenticado.close(); }

        BrowserContext anonimo = browser.newContext(
            new Browser.NewContextOptions().setBaseURL(lab.url()));
        try {
          APIResponse profile = anonimo.request().get("/api/profile");
          try { assertEquals(401, profile.status()); }
          finally { profile.dispose(); }
        } finally { anonimo.close(); }
      } finally {
        browser.close();
        Files.deleteIfExists(state);
      }
    }
  }

  @Test
  @Tag("solution-09")
  void aula09IntegraçãoMockEAbortFicamSeparados() throws Exception {
    try (ReferenceLab lab = ReferenceLab.start();
         ReferenceSession session = new ReferenceSession(lab.url())) {
      List<String> requests = new CopyOnWriteArrayList<>();
      session.page.onRequest(request -> requests.add(request.method() + " " + request.url()));
      session.page.navigate("/network");

      Response real = session.page.waitForResponse(
          response -> response.url().endsWith("/api/items")
              && response.request().method().equals("GET"),
          () -> session.page.getByText("Carregar itens").click());
      assertEquals(200, real.status());
      assertThat(session.page.getByTestId("result")).containsText("Backpack");
      assertTrue(requests.stream().anyMatch(value -> value.endsWith("/api/items")));

      session.page.route("**/api/items", route -> route.fulfill(new Route.FulfillOptions()
          .setStatus(200).setContentType("application/json").setBody("[]")));
      session.page.getByText("Carregar itens").click();
      assertThat(session.page.getByTestId("result")).hasText("items:[]");

      session.page.route("**/api/blocked", route -> route.abort());
      session.page.getByText("Requisição bloqueável").click();
      assertThat(session.page.getByTestId("result")).hasText("blocked:erro");
    }
  }

  private static final class LoginPage {
    private final Page page;
    LoginPage(Page page) { this.page = page; }
    void open() { page.navigate("/"); }
    void loginAs(String user, String password) {
      page.getByPlaceholder("Username").fill(user);
      page.getByPlaceholder("Password").fill(password);
      page.getByTestId("login-button").click();
    }
  }

  private static final class InventoryPage {
    private final Page page;
    InventoryPage(Page page) { this.page = page; }
    Locator title() { return page.getByTestId("title"); }
    Locator cartBadge() { return page.getByTestId("shopping-cart-badge"); }
    void add(String name) {
      page.getByTestId("inventory-item")
          .filter(new Locator.FilterOptions().setHasText(name))
          .getByRole(AriaRole.BUTTON,
              new Locator.GetByRoleOptions().setName("Add to cart")).click();
    }
  }
}
