package br.com.curso.playwright.aula12;

import br.com.curso.playwright.support.junit.PlaywrightTest;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

@Disabled("Projeto final: remova quando a suíte capstone estiver implementada")
@PlaywrightTest
@Tag("exercise-12")
class ProjetoFinalExercicioTest {
  @Test
  @Tag("smoke")
  @Tag("regression")
  void deveAutenticarEExibirCatalogo(Page page) {
    fail("Implemente a smoke com Page Objects");
  }

  @Test
  @Tag("smoke")
  @Tag("regression")
  void deveConcluirCompra(Page page) {
    fail("Implemente a compra completa");
  }

  @Test
  @Tag("negative")
  @Tag("regression")
  void deveValidarAutenticacaoECheckout(Page page) {
    fail("Implemente os cenários negativos");
  }
}
