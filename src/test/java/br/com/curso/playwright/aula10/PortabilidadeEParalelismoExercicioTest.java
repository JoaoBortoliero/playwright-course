package br.com.curso.playwright.aula10;

import br.com.curso.playwright.labs.CourseLabExtension;
import br.com.curso.playwright.labs.CourseLabServer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.fail;

@Disabled("Aula 10: remova apos implementar smoke, mobile e paralelismo")
@ExtendWith(CourseLabExtension.class)
@Tag("exercise-10")
class PortabilidadeEParalelismoExercicioTest {

    @Test
    @Tag("smoke")
    void deveExecutarSmokeNoEdgeInstalado(CourseLabServer lab) {
        // TODO:
        // 1. use CourseConfig e CourseBrowserFactory;
        // 2. utilize somente o Microsoft Edge instalado;
        // 3. valide heading, nome acessivel e foco por teclado;
        // 4. feche todos os recursos criados pelo teste.
        fail("Implemente a smoke no Edge instalado");
    }

    @Test
    @Tag("parallel")
    void deveExecutarSegundoCenarioDesktopIndependente(CourseLabServer lab) {
        // TODO:
        // 1. crie uma fixture independente;
        // 2. navegue pelo laboratorio;
        // 3. valide um estado observavel;
        // 4. nao compartilhe objetos Playwright.
        fail("Implemente o segundo cenario apto a paralelismo");
    }

    @Test
    @Tag("mobile")
    void deveEmularViewportMobileETouch(CourseLabServer lab) {
        // TODO:
        // 1. utilize o Microsoft Edge instalado;
        // 2. configure viewport 390 x 844, touch e isMobile;
        // 3. crie e feche um BrowserContext exclusivo.
        fail("Implemente a emulacao mobile no Edge");
    }
}
