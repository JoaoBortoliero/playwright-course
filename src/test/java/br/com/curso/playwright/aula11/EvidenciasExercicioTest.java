package br.com.curso.playwright.aula11;

import br.com.curso.playwright.support.config.CourseConfig;
import br.com.curso.playwright.support.junit.PlaywrightTest;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

@Disabled("Aula 11: remova após compreender a política de evidências")
@PlaywrightTest
@Tag("exercise-11")
class EvidenciasExercicioTest {
  @Test
  void deveProduzirTraceScreenshotEStepsUteis(Page page, CourseConfig config) {
    // TODO: Allure steps, ação/assertion local, anexo sem segredo e screenshot explicativa.
    fail("Implemente a Aula 11 e analise o trace gerado pela extensão");
  }
}
