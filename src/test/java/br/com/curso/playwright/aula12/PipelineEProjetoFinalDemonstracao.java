package br.com.curso.playwright.aula12;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("demo-12")
class PipelineEProjetoFinalDemonstracao {
  @Test
  void deveManterBibliotecaImagemEstagiosEAgendaComoUmContrato() throws Exception {
    String pom = Files.readString(Path.of("pom.xml"));
    String pipeline = Files.readString(Path.of("Jenkinsfile"));

    String version = "1.62.0";
    assertTrue(pom.contains("<playwright.version>" + version + "</playwright.version>"));
    assertTrue(pipeline.contains("playwright/java:v" + version + "-noble"));
    assertTrue(pipeline.contains("stage('Smoke cross-browser')"));
    assertTrue(pipeline.contains("stage('Regression Chromium')"));
    assertTrue(pipeline.contains("post {"));
    assertTrue(pipeline.contains("cron('H 2 * * *')"));
    assertTrue(pipeline.contains("disableConcurrentBuilds()"));

    long browsers = java.util.stream.Stream.of("chromium", "firefox", "webkit")
        .filter(name -> pipeline.contains("-Dbrowser=" + name))
        .count();
    assertEquals(3, browsers);
  }
}
