package br.com.curso.playwright.aula02;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

@Disabled("Aula 2: remova esta anotacao quando iniciar os exercicios")
class LoginNegativoExercicioTest {

  @Test
  @DisplayName("deve exibir erro para usuario bloqueado")
  void deveExibirErroParaUsuarioBloqueado() {
    // TODO Aula 2: use role, placeholder, test ID e assertion web-first.
    fail("Implemente o primeiro exercicio da Aula 2");
  }

  @Test
  @DisplayName("deve exigir o preenchimento do username")
  void deveExigirPreenchimentoDoUsername() {
    // TODO Aula 2: mantenha este cenario independente do teste anterior.
    fail("Implemente o segundo exercicio da Aula 2");
  }

  @Test
  @DisplayName("deve adicionar somente a mochila usando locator com escopo")
  void deveAdicionarSomenteMochilaUsandoLocatorComEscopo() {
    // TODO Aula 2: filtre o item pelo dominio, nao por posicao.
    fail("Implemente o desafio independente da Aula 2");
  }
}
