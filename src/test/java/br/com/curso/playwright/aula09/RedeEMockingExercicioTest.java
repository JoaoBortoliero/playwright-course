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

@Disabled("Aula 9: remova apos separar integracao real, mock e falha de rede")
@PlaywrightTest
@ExtendWith(CourseLabExtension.class)
@Tag("exercise-09")
class RedeEMockingExercicioTest {

    @Test
    void deveObservarRespostaReal(Page page, CourseLabServer lab) {
        // TODO:
        // 1. registre os requests;
        // 2. espere a resposta GET /api/items;
        // 3. valide status, metodo, URL e consequencia na UI.
        fail("Implemente a observacao real");
    }

    @Test
    void deveCumprirContratoComMock(Page page, CourseLabServer lab) {
        // TODO:
        // 1. registre route/fulfill antes da acao;
        // 2. responda um payload controlado;
        // 3. valide a consequencia na UI.
        fail("Implemente o contrato simulado");
    }

    @Test
    void deveTratarFalhaDeRede(Page page, CourseLabServer lab) {
        // TODO:
        // 1. utilize route/abort e valide o estado da UI;
        // 2. crie uma pagina local com setContent;
        // 3. capture e valide um page error controlado.
        fail("Implemente a falha de transporte");
    }
}
