package br.com.curso.playwright.support.config;

import java.net.URI;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;

/** Configuração final da suíte. Propriedade JVM > variável de ambiente > padrão. */
public record CourseConfig(
    String baseUrl,
    String apiBaseUrl,
    String browser,
    boolean headless,
    double timeoutMs,
    Path artifactsDir,
    EvidencePolicy trace,
    EvidencePolicy video,
    EvidencePolicy screenshot,
    String tags,
    String environment) {

  public enum EvidencePolicy { OFF, ON_FAILURE, ALWAYS }

  private static final Set<String> BROWSERS = Set.of("chromium", "firefox", "webkit");

  public CourseConfig {
    validateUrl("baseUrl", baseUrl);
    if (apiBaseUrl != null && !apiBaseUrl.isBlank()) validateUrl("apiBaseUrl", apiBaseUrl);
    browser = browser.toLowerCase(Locale.ROOT);
    if (!BROWSERS.contains(browser)) {
      throw new IllegalArgumentException("browser deve ser chromium, firefox ou webkit: " + browser);
    }
    if (timeoutMs <= 0) throw new IllegalArgumentException("timeout deve ser maior que zero");
    if (artifactsDir == null) throw new IllegalArgumentException("artifactsDir é obrigatório");
  }

  public static CourseConfig load() {
    return new CourseConfig(
        value("baseUrl", "COURSE_BASE_URL", "https://www.saucedemo.com/"),
        value("apiBaseUrl", "COURSE_API_BASE_URL", ""),
        value("browser", "COURSE_BROWSER", "chromium"),
        boolValue("headless", "COURSE_HEADLESS", true),
        doubleValue("timeout", "COURSE_TIMEOUT", 10_000),
        Path.of(value("artifactsDir", "COURSE_ARTIFACTS_DIR", "artifacts")),
        policy("trace", "COURSE_TRACE", EvidencePolicy.ON_FAILURE),
        policy("video", "COURSE_VIDEO", EvidencePolicy.OFF),
        policy("screenshot", "COURSE_SCREENSHOT", EvidencePolicy.ON_FAILURE),
        value("tags", "COURSE_TAGS", ""),
        value("environment", "COURSE_ENVIRONMENT", "local"));
  }

  private static String value(String property, String environment, String fallback) {
    String system = System.getProperty(property);
    if (system != null && !system.isBlank()) return system;
    String env = System.getenv(environment);
    return env == null || env.isBlank() ? fallback : env;
  }

  private static boolean boolValue(String property, String environment, boolean fallback) {
    String value = value(property, environment, Boolean.toString(fallback));
    if (!Set.of("true", "false").contains(value.toLowerCase(Locale.ROOT))) {
      throw new IllegalArgumentException(property + " deve ser true ou false: " + value);
    }
    return Boolean.parseBoolean(value);
  }

  private static double doubleValue(String property, String environment, double fallback) {
    try {
      return Double.parseDouble(value(property, environment, Double.toString(fallback)));
    } catch (NumberFormatException exception) {
      throw new IllegalArgumentException(property + " deve ser numérico", exception);
    }
  }

  private static EvidencePolicy policy(
      String property, String environment, EvidencePolicy fallback) {
    return switch (value(property, environment, fallback.name()).toLowerCase(Locale.ROOT)
        .replace('_', '-')) {
      case "off" -> EvidencePolicy.OFF;
      case "on-failure" -> EvidencePolicy.ON_FAILURE;
      case "always" -> EvidencePolicy.ALWAYS;
      default -> throw new IllegalArgumentException(property + " deve ser off, on-failure ou always");
    };
  }

  private static void validateUrl(String name, String value) {
    try {
      String scheme = URI.create(value).getScheme();
      if (!Set.of("http", "https").contains(scheme)) throw new IllegalArgumentException();
    } catch (RuntimeException exception) {
      throw new IllegalArgumentException(name + " deve ser uma URL http(s): " + value, exception);
    }
  }
}
