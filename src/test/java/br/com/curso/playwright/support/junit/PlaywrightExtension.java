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
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Fixture final: runtime/browser por thread e contexto/pagina por teste. */
public final class PlaywrightExtension
        implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

    private static final ExtensionContext.Namespace NS =
            ExtensionContext.Namespace.create(PlaywrightExtension.class);

    private static final String SESSION_KEY = "session";
    private static final String RUNTIMES_KEY = "runtimes";

    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        Runtime runtime = registry(extensionContext).forCurrentThread();
        CourseConfig config = runtime.config;
        Files.createDirectories(config.artifactsDir());

        Browser.NewContextOptions options =
                new Browser.NewContextOptions().setBaseURL(config.baseUrl());

        if (config.video() != CourseConfig.EvidencePolicy.OFF) {
            Path videosDirectory = config.artifactsDir().resolve("videos");
            Files.createDirectories(videosDirectory);
            options.setRecordVideoDir(videosDirectory);
        }

        BrowserContext browserContext = null;

        try {
            browserContext = runtime.browser.newContext(options);
            browserContext.setDefaultTimeout(config.timeoutMs());
            browserContext.setDefaultNavigationTimeout(config.timeoutMs());

            if (config.trace() != CourseConfig.EvidencePolicy.OFF) {
                browserContext.tracing().start(
                        new Tracing.StartOptions()
                                .setScreenshots(true)
                                .setSnapshots(true)
                                .setSources(true));
            }

            Page page = browserContext.newPage();
            extensionContext.getStore(NS).put(
                    SESSION_KEY,
                    new Session(runtime, browserContext, page));
        } catch (Exception initializationError) {
            if (browserContext != null) {
                try {
                    browserContext.close();
                } catch (Exception closeError) {
                    initializationError.addSuppressed(closeError);
                }
            }
            throw initializationError;
        }
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) throws Exception {
        Session session = extensionContext.getStore(NS)
                .remove(SESSION_KEY, Session.class);

        if (session == null) {
            return;
        }

        boolean failed = extensionContext.getExecutionException().isPresent();
        Throwable evidenceError = null;
        Throwable cleanupError = null;

        String readable = extensionContext.getDisplayName()
                .replaceAll("[^a-zA-Z0-9.-]", "_");
        if (readable.length() > 60) {
            readable = readable.substring(0, 60);
        }

        String safeName = readable + "-"
                + Integer.toHexString(extensionContext.getUniqueId().hashCode());
        Path directory = session.runtime.config.artifactsDir();

        Video video = session.runtime.config.video()
                == CourseConfig.EvidencePolicy.OFF
                ? null
                : session.page.video();

        try {
            captureScreenshot(session, failed, directory, safeName);
            stopTrace(session, failed, directory, safeName);
        } catch (Throwable error) {
            evidenceError = error;
        } finally {
            try {
                session.context.close();
            } catch (Throwable error) {
                cleanupError = error;
            }

            try {
                if (session.request != null) {
                    session.request.dispose();
                }
            } catch (Throwable error) {
                cleanupError = combine(cleanupError, error);
            }
        }

        if (video != null) {
            try {
                processVideo(session, video, failed);
            } catch (Throwable error) {
                evidenceError = combine(evidenceError, error);
            }
        }

        if (!failed) {
            Throwable finalError = combine(evidenceError, cleanupError);
            if (finalError != null) {
                rethrow(finalError);
            }
        }
    }

    @Override
    public boolean supportsParameter(
            ParameterContext parameterContext,
            ExtensionContext extensionContext) {

        Class<?> type = parameterContext.getParameter().getType();
        return type == Page.class
                || type == BrowserContext.class
                || type == CourseConfig.class
                || type == APIRequestContext.class;
    }

    @Override
    public Object resolveParameter(
            ParameterContext parameterContext,
            ExtensionContext extensionContext) {

        Session session = extensionContext.getStore(NS)
                .get(SESSION_KEY, Session.class);

        if (session == null) {
            throw new ParameterResolutionException(
                    "A sessao Playwright nao foi inicializada.");
        }

        Class<?> type = parameterContext.getParameter().getType();

        if (type == Page.class) {
            return session.page;
        }
        if (type == BrowserContext.class) {
            return session.context;
        }
        if (type == CourseConfig.class) {
            return session.runtime.config;
        }

        if (session.request == null) {
            String baseUrl = session.runtime.config.apiBaseUrl();
            APIRequest.NewContextOptions options =
                    new APIRequest.NewContextOptions();

            if (baseUrl != null && !baseUrl.isBlank()) {
                options.setBaseURL(baseUrl);
            }

            session.request = session.runtime.playwright
                    .request()
                    .newContext(options);
        }

        return session.request;
    }

    private static void captureScreenshot(
            Session session,
            boolean failed,
            Path directory,
            String safeName) {

        if (!shouldKeep(session.runtime.config.screenshot(), failed)) {
            return;
        }

        byte[] image = session.page.screenshot(
                new Page.ScreenshotOptions()
                        .setPath(directory.resolve(safeName + ".png"))
                        .setFullPage(true));

        Allure.addAttachment(
                "Screenshot",
                "image/png",
                new ByteArrayInputStream(image),
                ".png");
    }

    private static void stopTrace(
            Session session,
            boolean failed,
            Path directory,
            String safeName) {

        if (session.runtime.config.trace() == CourseConfig.EvidencePolicy.OFF) {
            return;
        }

        Tracing.StopOptions options = new Tracing.StopOptions();
        if (shouldKeep(session.runtime.config.trace(), failed)) {
            options.setPath(directory.resolve(safeName + "-trace.zip"));
        }

        session.context.tracing().stop(options);
    }

    private static void processVideo(
            Session session,
            Video video,
            boolean failed) throws Exception {

        Path videoPath = video.path();

        if (shouldKeep(session.runtime.config.video(), failed)) {
            try (InputStream input = Files.newInputStream(videoPath)) {
                Allure.addAttachment(
                        "Video",
                        "video/webm",
                        input,
                        ".webm");
            }
        } else {
            Files.deleteIfExists(videoPath);
        }
    }

    private static boolean shouldKeep(
            CourseConfig.EvidencePolicy policy,
            boolean failed) {

        return policy == CourseConfig.EvidencePolicy.ALWAYS
                || (policy == CourseConfig.EvidencePolicy.ON_FAILURE && failed);
    }

    private static RuntimeRegistry registry(ExtensionContext context) {
        ExtensionContext.Store rootStore = context.getRoot().getStore(NS);
        return rootStore.getOrComputeIfAbsent(
                RUNTIMES_KEY,
                key -> new RuntimeRegistry(),
                RuntimeRegistry.class);
    }

    private static Throwable combine(Throwable current, Throwable additional) {
        if (current == null) {
            return additional;
        }
        current.addSuppressed(additional);
        return current;
    }

    private static void rethrow(Throwable error) throws Exception {
        if (error instanceof Exception exception) {
            throw exception;
        }
        if (error instanceof Error seriousError) {
            throw seriousError;
        }
        throw new RuntimeException(error);
    }

    private static final class RuntimeRegistry
            implements ExtensionContext.Store.CloseableResource {

        private final Map<Long, Runtime> runtimes = new ConcurrentHashMap<>();

        Runtime forCurrentThread() {
            return runtimes.computeIfAbsent(
                    Thread.currentThread().getId(),
                    ignored -> new Runtime());
        }

        @Override
        public void close() {
            RuntimeException closeFailure = null;

            for (Runtime runtime : runtimes.values()) {
                try {
                    runtime.close();
                } catch (RuntimeException error) {
                    if (closeFailure == null) {
                        closeFailure = error;
                    } else {
                        closeFailure.addSuppressed(error);
                    }
                }
            }

            runtimes.clear();

            if (closeFailure != null) {
                throw closeFailure;
            }
        }
    }

    private static final class Runtime {
        final CourseConfig config;
        final Playwright playwright;
        final Browser browser;

        Runtime() {
            config = CourseConfig.load();
            playwright = Playwright.create();

            Browser launchedBrowser = null;
            try {
                playwright.selectors().setTestIdAttribute("data-test");
                PlaywrightAssertions.setDefaultAssertionTimeout(
                        config.timeoutMs());
                launchedBrowser = CourseBrowserFactory.launch(
                        playwright,
                        config);
                browser = launchedBrowser;
            } catch (RuntimeException error) {
                if (launchedBrowser != null) {
                    try {
                        launchedBrowser.close();
                    } catch (RuntimeException closeError) {
                        error.addSuppressed(closeError);
                    }
                }

                try {
                    playwright.close();
                } catch (RuntimeException closeError) {
                    error.addSuppressed(closeError);
                }

                throw error;
            }
        }

        void close() {
            RuntimeException closeFailure = null;

            try {
                browser.close();
            } catch (RuntimeException error) {
                closeFailure = error;
            }

            try {
                playwright.close();
            } catch (RuntimeException error) {
                if (closeFailure == null) {
                    closeFailure = error;
                } else {
                    closeFailure.addSuppressed(error);
                }
            }

            if (closeFailure != null) {
                throw closeFailure;
            }
        }
    }

    private static final class Session {
        final Runtime runtime;
        final BrowserContext context;
        final Page page;
        APIRequestContext request;

        Session(
                Runtime runtime,
                BrowserContext context,
                Page page) {

            this.runtime = runtime;
            this.context = context;
            this.page = page;
        }
    }
}
