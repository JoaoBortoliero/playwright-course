package br.com.curso.playwright.aula07;

import br.com.curso.playwright.support.junit.PlaywrightTest;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@PlaywrightTest
@Tag("demo-07")
class ArquiteturaDemonstracao {
    @Test
    void testeExpressaRegraEObjetoEncapsulaMecanica(Page page) {
        page.setContent("<main><h1>Login</h1><label>Usuário <input></label><button>Entrar</button><p data-test='state'></p><script>document.querySelector('button').onclick=()=>document.querySelector('[data-test=state]').textContent='Autenticado'</script></main>");
        LoginPanel login = new LoginPanel(page);
        login.loginAs("student");
        assertThat(login.state()).hasText("Autenticado");
    }

    private static final class LoginPanel {
        private final Page page;

        LoginPanel(Page page) {
            this.page = page;
        }

        void loginAs(String user) {
            page.getByLabel("Usuário").fill(user);
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Entrar")).click();
        }

        Locator state() {
            return page.getByTestId("state");
        }
    }
}
