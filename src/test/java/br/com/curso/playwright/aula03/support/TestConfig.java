package br.com.curso.playwright.aula03.support;

import java.net.URI;
import java.util.Locale;
import java.util.Set;

public record TestConfig(
        String baseUrl,
        boolean headless,
        double timeoutMs) {

    public TestConfig {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("baseUrl nao pode ser vazia.");
        }

        URI uri;

        try {
            uri = URI.create(baseUrl);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "baseUrl invalida: " + baseUrl,
                    exception
            );
        }

        if (!Set.of("http", "https").contains(uri.getScheme())) {
            throw new IllegalArgumentException(
                    "baseUrl deve usar http ou https: " + baseUrl
            );
        }

        if (timeoutMs <= 0) {
            throw new IllegalArgumentException(
                    "timeout deve ser maior que zero."
            );
        }
    }

    public static TestConfig fromSystemProperties() {
        return new TestConfig(
                System.getProperty(
                        "baseUrl",
                        "https://www.saucedemo.com/"
                ),
                booleanProperty("headless", false),
                doubleProperty("timeout", 10_000)
        );
    }

    private static boolean booleanProperty(
            String name,
            boolean defaultValue) {

        String rawValue = System.getProperty(name);

        if (rawValue == null) {
            return defaultValue;
        }

        return switch (rawValue.toLowerCase(Locale.ROOT)) {
            case "true" -> true;
            case "false" -> false;
            default -> throw new IllegalArgumentException(
                    name + " deve ser true ou false, mas recebeu: " + rawValue
            );
        };
    }

    private static double doubleProperty(
            String name,
            double defaultValue) {

        String rawValue = System.getProperty(name);

        if (rawValue == null) {
            return defaultValue;
        }

        try {
            return Double.parseDouble(rawValue);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    name + " deve ser numerico, mas recebeu: " + rawValue,
                    exception
            );
        }
    }
}
