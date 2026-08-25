package br.com.curso.playwright.support.junit;

import br.com.curso.playwright.support.config.CourseBrowserFactory;
import br.com.curso.playwright.support.config.CourseConfig;
import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Tracing;
import com.microsoft.playwright.Video;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import io.qameta.allure.Allure;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Fixture final: runtime/browser por thread e contexto/página por teste. */
public final class PlaywrightExtension
    implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

  private static final ExtensionContext.Namespace NS =
      ExtensionContext.Namespace.create(PlaywrightExtension.class);

  @Override
  public void beforeEach(ExtensionContext context) throws Exception {
    RuntimeRegistry registry = registry(context);
    Runtime runtime = registry.forCurrentThread();
    CourseConfig config = runtime.config;
    Files.createDirectories(config.artifactsDir());

    Browser.NewContextOptions options = new Browser.NewContextOptions().setBaseURL(config.baseUrl());
    if (config.video() != CourseConfig.EvidencePolicy.OFF) {
      options.setRecordVideoDir(config.artifactsDir().resolve("videos"));
    }
    BrowserContext browserContext = runtime.browser.newContext(options);
    browserContext.setDefaultTimeout(config.timeoutMs());
    browserContext.setDefaultNavigationTimeout(config.timeoutMs());
    Page page = browserContext.newPage();

    if (config.trace() != CourseConfig.EvidencePolicy.OFF) {
      browserContext.tracing().start(new Tracing.StartOptions()
          .setScreenshots(true).setSnapshots(true).setSources(true));
    }
    context.getStore(NS).put("session", new Session(runtime, browserContext, page));
  }

  @Override
  public void afterEach(ExtensionContext context) throws Exception {
    Session session = context.getStore(NS).remove("session", Session.class);
    if (session == null) return;
    boolean failed = context.getExecutionException().isPresent();
    String readable = context.getDisplayName().replaceAll("[^a-zA-Z0-9.-]", "_");
    if (readable.length() > 60) readable = readable.substring(0, 60);
    String safeName = readable + "-" + Integer.toHexString(context.getUniqueId().hashCode());
    Path directory = session.runtime.config.artifactsDir();
    Video video = session.runtime.config.video() == CourseConfig.EvidencePolicy.OFF
        ? null : session.page.video();
    try {
      if (shouldKeep(session.runtime.config.screenshot(), failed)) {
        byte[] image = session.page.screenshot(new Page.ScreenshotOptions()
            .setPath(directory.resolve(safeName + ".png")).setFullPage(true));
        Allure.addAttachment("Screenshot", "image/png", new ByteArrayInputStream(image), ".png");
      }
      if (session.runtime.config.trace() != CourseConfig.EvidencePolicy.OFF) {
        Tracing.StopOptions stop = new Tracing.StopOptions();
        if (shouldKeep(session.runtime.config.trace(), failed)) {
          stop.setPath(directory.resolve(safeName + "-trace.zip"));
        }
        session.context.tracing().stop(stop);
      }
    } finally {
      try {
        session.context.close();
      } finally {
        if (session.request != null) session.request.dispose();
      }
      if (video != null) {
        try {
          Path videoPath = video.path();
          if (shouldKeep(session.runtime.config.video(), failed)) {
            Allure.addAttachment("Vídeo", "video/webm", Files.newInputStream(videoPath), ".webm");
          } else {
            Files.deleteIfExists(videoPath);
          }
        } catch (Exception evidenceError) {
          if (!failed) throw evidenceError;
        }
      }
    }
  }

  @Override
  public boolean supportsParameter(ParameterContext parameter, ExtensionContext context) {
    Class<?> type = parameter.getParameter().getType();
    return type == Page.class || type == BrowserContext.class || type == CourseConfig.class
        || type == APIRequestContext.class;
  }

  @Override
  public Object resolveParameter(ParameterContext parameter, ExtensionContext context) {
    Session session = context.getStore(NS).get("session", Session.class);
    Class<?> type = parameter.getParameter().getType();
    if (type == Page.class) return session.page;
    if (type == BrowserContext.class) return session.context;
    if (type == CourseConfig.class) return session.runtime.config;
    if (session.request == null) {
      String base = session.runtime.config.apiBaseUrl();
      APIRequest.NewContextOptions options = new APIRequest.NewContextOptions();
      if (base != null && !base.isBlank()) options.setBaseURL(base);
      session.request = session.runtime.playwright.request().newContext(options);
    }
    return session.request;
  }

  private static boolean shouldKeep(CourseConfig.EvidencePolicy policy, boolean failed) {
    return policy == CourseConfig.EvidencePolicy.ALWAYS
        || (policy == CourseConfig.EvidencePolicy.ON_FAILURE && failed);
  }

  private static RuntimeRegistry registry(ExtensionContext context) {
    ExtensionContext.Store root = context.getRoot().getStore(NS);
    return root.getOrComputeIfAbsent("runtimes", key -> new RuntimeRegistry(), RuntimeRegistry.class);
  }

  private static final class RuntimeRegistry implements AutoCloseable {
    private final Map<Long, Runtime> runtimes = new ConcurrentHashMap<>();
    Runtime forCurrentThread() {
      return runtimes.computeIfAbsent(Thread.currentThread().getId(), ignored -> new Runtime());
    }
    @Override public void close() { runtimes.values().forEach(Runtime::close); }
  }

  private static final class Runtime {
    final CourseConfig config = CourseConfig.load();
    final Playwright playwright = Playwright.create();
    final Browser browser;
    Runtime() {
      playwright.selectors().setTestIdAttribute("data-test");
      PlaywrightAssertions.setDefaultAssertionTimeout(config.timeoutMs());
      browser = CourseBrowserFactory.launch(playwright, config);
    }
    void close() { browser.close(); playwright.close(); }
  }

  private static final class Session {
    final Runtime runtime;
    final BrowserContext context;
    final Page page;
    APIRequestContext request;
    Session(Runtime runtime, BrowserContext context, Page page) {
      this.runtime = runtime; this.context = context; this.page = page;
    }
  }
}
