package br.com.curso.playwright.aula10;

import br.com.curso.playwright.labs.CourseLabExtension;
import br.com.curso.playwright.labs.CourseLabServer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.fail;

@Disabled("Aula 10: remova após smoke portável e mobile isolado")
@ExtendWith(CourseLabExtension.class)
@Tag("exercise-10")
class PortabilidadeEParalelismoExercicioTest {
  @Test
  @Tag("smoke")
  @Tag("cross-browser")
  void deveExecutarSmokePortavel(CourseLabServer lab) {
    // TODO: use CourseConfig/Factory; não fixe o motor; valide heading e foco por teclado.
    fail("Implemente a smoke cross-browser");
  }

  @Test
  @Tag("mobile")
  void deveEmularViewportMobileETouch(CourseLabServer lab) {
    // TODO: Chromium, setViewportSize(390, 844), touch/isMobile e contexto fechado.
    fail("Implemente a emulação mobile");
  }
}
