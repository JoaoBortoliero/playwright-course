package br.com.curso.playwright.aula11;

import br.com.curso.playwright.support.config.CourseConfig;
import br.com.curso.playwright.support.junit.PlaywrightTest;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

@Disabled("Aula 11: remova apos compreender a politica de evidencias")
@PlaywrightTest
@Tag("exercise-11")
class EvidenciasExercicioTest {

    @Test
    void deveProduzirTraceScreenshotEStepsUteis(
            Page page,
            CourseConfig config) {

        // TODO:
        // 1. crie steps Allure para preparar, agir e verificar;
        // 2. realize uma acao e uma assertion local;
        // 3. anexe texto diagnostico sem segredo;
        // 4. capture uma screenshot explicativa;
        // 5. analise o trace gerado pela extensao.

        fail("Implemente a Aula 11 e analise as evidencias geradas");
    }
}
