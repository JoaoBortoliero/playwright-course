package br.com.curso.playwright.aula12;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("demo-12")
class PipelineEProjetoFinalDemonstracao {
  @Test
  void deveManterEdgePipelineEAgendaComoUmContrato() throws Exception {
    String pom = Files.readString(Path.of("pom.xml"));
    String dockerfile = Files.readString(Path.of("Dockerfile"));
    String pipeline = Files.readString(Path.of("Jenkinsfile"));
    String version = "1.62.0";

    assertTrue(pom.contains("<playwright.version>" + version + "</playwright.version>"));
    assertTrue(dockerfile.contains("playwright/java:v" + version + "-noble"));
    assertTrue(dockerfile.contains("microsoft-edge-stable"));
    assertTrue(dockerfile.contains("PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1"));
    assertTrue(pipeline.contains("stage('Smoke Edge')"));
    assertTrue(pipeline.contains("stage('Regression Edge')"));
    assertTrue(pipeline.contains("pwsh -File ./course.ps1"));
    assertTrue(pipeline.contains("cron('H 2 * * *')"));
    assertTrue(pipeline.contains("disableConcurrentBuilds()"));
    assertTrue(pipeline.contains("post {"));
    assertFalse(pipeline.contains("-Browser"));
    assertFalse(pipeline.contains("-Dbrowser"));
  }
}
