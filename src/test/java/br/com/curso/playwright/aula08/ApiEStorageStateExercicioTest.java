package br.com.curso.playwright.aula08;

import br.com.curso.playwright.labs.CourseLabExtension;
import br.com.curso.playwright.labs.CourseLabServer;
import com.microsoft.playwright.APIRequestContext;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.fail;

@Disabled("Aula 8: remova após implementar API e estado sem versionar a sessão")
@ExtendWith(CourseLabExtension.class)
@Tag("exercise-08")
class ApiEStorageStateExercicioTest {
  @Test
  void deveValidarGetPostEErroDaApi(CourseLabServer lab) {
    // TODO: crie Playwright/APIRequestContext; valide GET, POST e 503; dispose tudo.
    APIRequestContext request = null;
    fail("Implemente os contratos HTTP");
  }

  @Test
  void deveReutilizarFotografiaDeAutenticacaoEmContextoNovo(CourseLabServer lab) {
    // TODO: login UI, storageState em temp, novo contexto, profile 200 e anônimo 401.
    fail("Implemente storage state e isolamento");
  }
}
