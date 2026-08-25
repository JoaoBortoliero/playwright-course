package br.com.curso.playwright.aula01;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

@Disabled("Aula 1: remova esta anotacao quando iniciar o exercicio")
class LoginSauceDemoExercicioTest {

  @Test
  @DisplayName("deve autenticar o usuario padrao")
  void deveAutenticarUsuarioPadrao() {
    // TODO Aula 1:
    // 1. crie Playwright, Chromium, BrowserContext e Page;
    // 2. navegue para o SauceDemo e autentique o usuario padrao;
    // 3. valide a URL e o titulo Products com assertions web-first;
    // 4. feche os recursos mesmo quando uma assertion falhar.
    fail("Implemente o exercicio da Aula 1");
  }
}
