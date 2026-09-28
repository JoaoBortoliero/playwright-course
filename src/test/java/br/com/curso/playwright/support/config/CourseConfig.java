package br.com.curso.playwright.support.config;

import java.net.URI;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;

/** Configuracao final da suite. Propriedade JVM > variavel de ambiente > padrao. */
public record CourseConfig(
        String baseUrl,
        String apiBaseUrl,
        boolean headless,
        double timeoutMs,
        Path artifactsDir,
        EvidencePolicy trace,
        EvidencePolicy video,
        EvidencePolicy screenshot,
        String tags,
        String environment) {

    public enum EvidencePolicy {
        OFF,
        ON_FAILURE,
        ALWAYS
    }

    public CourseConfig {
        validateUrl("baseUrl", baseUrl);

        if (apiBaseUrl != null && !apiBaseUrl.isBlank()) {
            validateUrl("apiBaseUrl", apiBaseUrl);
        }

        if (timeoutMs <= 0) {
            throw new IllegalArgumentException("timeout deve ser maior que zero");
        }

        if (artifactsDir == null) {
            throw new IllegalArgumentException("artifactsDir e obrigatorio");
        }

        if (trace == null || video == null || screenshot == null) {
            throw new IllegalArgumentException("As politicas de evidencia sao obrigatorias");
        }

        tags = tags == null ? "" : tags.trim();
        environment = environment == null || environment.isBlank()
                ? "local"
                : environment.trim();
    }

    public static CourseConfig load() {
        return new CourseConfig(
                value("baseUrl", "COURSE_BASE_URL", "https://www.saucedemo.com/"),
                value("apiBaseUrl", "COURSE_API_BASE_URL", ""),
                boolValue("headless", "COURSE_HEADLESS", false),
                doubleValue("timeout", "COURSE_TIMEOUT", 10_000),
                Path.of(value("artifactsDir", "COURSE_ARTIFACTS_DIR", "artifacts")),
                policy("trace", "COURSE_TRACE", EvidencePolicy.ON_FAILURE),
                policy("video", "COURSE_VIDEO", EvidencePolicy.OFF),
                policy("screenshot", "COURSE_SCREENSHOT", EvidencePolicy.ON_FAILURE),
                value("tags", "COURSE_TAGS", ""),
                value("environment", "COURSE_ENVIRONMENT", "local")
        );
    }

    private static String value(
            String property,
            String environment,
            String fallback) {

        String system = System.getProperty(property);
        if (system != null && !system.isBlank()) {
            return system;
        }

        String env = System.getenv(environment);
        return env == null || env.isBlank() ? fallback : env;
    }

    private static boolean boolValue(
            String property,
            String environment,
            boolean fallback) {

        String value = value(property, environment, Boolean.toString(fallback));
        String normalized = value.toLowerCase(Locale.ROOT);

        if (!Set.of("true", "false").contains(normalized)) {
            throw new IllegalArgumentException(
                    property + " deve ser true ou false: " + value);
        }

        return Boolean.parseBoolean(normalized);
    }

    private static double doubleValue(
            String property,
            String environment,
            double fallback) {

        try {
            return Double.parseDouble(
                    value(property, environment, Double.toString(fallback)));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    property + " deve ser numerico",
                    exception);
        }
    }

    private static EvidencePolicy policy(
            String property,
            String environment,
            EvidencePolicy fallback) {

        String normalized = value(property, environment, fallback.name())
                .toLowerCase(Locale.ROOT)
                .replace('_', '-');

        return switch (normalized) {
            case "off" -> EvidencePolicy.OFF;
            case "on-failure" -> EvidencePolicy.ON_FAILURE;
            case "always" -> EvidencePolicy.ALWAYS;
            default -> throw new IllegalArgumentException(
                    property + " deve ser off, on-failure ou always");
        };
    }

    private static void validateUrl(String name, String value) {
        try {
            String scheme = URI.create(value).getScheme();
            if (!Set.of("http", "https").contains(scheme)) {
                throw new IllegalArgumentException();
            }
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException(
                    name + " deve ser uma URL http(s): " + value,
                    exception);
        }
    }
}
