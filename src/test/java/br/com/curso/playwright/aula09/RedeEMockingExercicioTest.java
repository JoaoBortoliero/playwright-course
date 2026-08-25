package br.com.curso.playwright.aula09;

import br.com.curso.playwright.labs.CourseLabExtension;
import br.com.curso.playwright.labs.CourseLabServer;
import br.com.curso.playwright.support.junit.PlaywrightTest;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.fail;

@Disabled("Aula 9: remova após separar integração real, mock e falha de rede")
@PlaywrightTest
@ExtendWith(CourseLabExtension.class)
@Tag("exercise-09")
class RedeEMockingExercicioTest {
  @Test
  void deveObservarRespostaReal(Page page, CourseLabServer lab) {
    // TODO: listener + waitForResponse específico + status + consequência na UI.
    fail("Implemente a observação real");
  }

  @Test
  void deveCumprirContratoComMock(Page page, CourseLabServer lab) {
    // TODO: registre route/fulfill antes da ação e deixe o mock explícito no teste.
    fail("Implemente o contrato simulado");
  }

  @Test
  void deveTratarFalhaDeRede(Page page, CourseLabServer lab) {
    // TODO: route/abort + estado da UI; depois capture um page error controlado.
    fail("Implemente a falha de transporte");
  }
}
