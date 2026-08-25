package br.com.curso.playwright.aula03;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

@Disabled("Exercicios do aluno: remova esta anotacao quando o lifecycle estiver completo")
class LifecycleEConfiguracaoExercicioTest {

  @Test
  @DisplayName("deve autenticar o usuario padrao")
  void deveAutenticarUsuarioPadrao() {
    // TODO Aula 3: use Page e TestConfig preparados pelos hooks.
    fail("Exercicio ainda nao implementado");
  }

  @Test
  @DisplayName("deve exibir erro para usuario bloqueado")
  void deveExibirErroParaUsuarioBloqueado() {
    // TODO Aula 3: este teste deve receber um contexto novo automaticamente.
    fail("Exercicio ainda nao implementado");
  }

  @Test
  @DisplayName("deve iniciar com sessao limpa")
  void deveIniciarComSessaoLimpa() {
    // TODO Aula 3: prove que o teste comeca na tela de login.
    fail("Exercicio ainda nao implementado");
  }
}
