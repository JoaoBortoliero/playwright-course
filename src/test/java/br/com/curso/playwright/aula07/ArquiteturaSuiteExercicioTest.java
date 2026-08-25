package br.com.curso.playwright.aula07;

import br.com.curso.playwright.support.junit.PlaywrightTest;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

@Disabled("Aula 7: remova após implementar os Page Objects starters")
@PlaywrightTest
@Tag("exercise-07")
class ArquiteturaSuiteExercicioTest {
  @Test
  void deveAutenticarUsuarioPadraoComObjetosDeDominio(Page page) {
    // TODO: LoginPage recebe page; teste mantém a assertion sobre Products.
    fail("Implemente LoginPage e o primeiro teste arquitetural");
  }

  @Test
  void deveComprarProdutoComComponentesReutilizaveis(Page page) {
    // TODO: InventoryPage, HeaderComponent, CartPage, CheckoutPage e Customer.
    fail("Implemente a jornada com objetos de domínio");
  }
}
