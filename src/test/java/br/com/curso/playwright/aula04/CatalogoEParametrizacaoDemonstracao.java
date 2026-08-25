package br.com.curso.playwright.aula04;

import br.com.curso.playwright.aula03.support.BrowserFactory;
import br.com.curso.playwright.aula03.support.TestConfig;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("demo-04")
class CatalogoEParametrizacaoDemonstracao {
  private TestConfig config;
  private Playwright playwright;
  private Browser browser;
  private BrowserContext context;
  private Page page;

  @BeforeAll
  void iniciarBrowser() {
    config = TestConfig.fromSystemProperties();
    playwright = Playwright.create();
    playwright.selectors().setTestIdAttribute("data-test");
    PlaywrightAssertions.setDefaultAssertionTimeout(config.timeoutMs());
    browser = BrowserFactory.launch(playwright, config);
  }

  @BeforeEach
  void criarContextoEPagina() {
    context = browser.newContext();
    context.setDefaultTimeout(config.timeoutMs());
    page = context.newPage();
  }

  @AfterEach
  void fecharContexto() {
    if (context != null) {
      context.close();
      context = null;
      page = null;
    }
  }

  @AfterAll
  void fecharBrowser() {
    if (browser != null) browser.close();
    if (playwright != null) playwright.close();
  }

  @Test
  void deveCompararPrecosSemAlterarAEvidencia() {
    page.setContent("""
        <label>Ordenação
          <select data-test="sort" onchange="ordenar(this.value)">
            <option value="original">Original</option>
            <option value="lohi">Menor preço</option>
          </select>
        </label>
        <ul data-test="catalog">
          <li data-test="price" data-value="29.99">$29.99</li>
          <li data-test="price" data-value="9.99">$9.99</li>
          <li data-test="price" data-value="15.99">$15.99</li>
        </ul>
        <script>
          function ordenar(value) {
            if (value !== 'lohi') return;
            const list = document.querySelector('[data-test=catalog]');
            [...list.children]
              .sort((a, b) => Number(a.dataset.value) - Number(b.dataset.value))
              .forEach(item => list.appendChild(item));
          }
        </script>
        """);

    page.getByTestId("sort").selectOption("lohi");
    assertThat(page.getByTestId("price")).hasCount(3);

    List<BigDecimal> exibidos = page.getByTestId("price").allTextContents()
        .stream().map(CatalogoEParametrizacaoDemonstracao::preco).toList();
    List<BigDecimal> esperados = new ArrayList<>(exibidos);
    esperados.sort(Comparator.naturalOrder());

    assertEquals(esperados, exibidos);
  }

  @ParameterizedTest(name = "usuário {0} produz {1}")
  @MethodSource("casosDeLogin")
  void deveExecutarUmCasoParaCadaLinhaDeDados(String usuario, String estadoEsperado) {
    page.setContent("""
        <label>Usuário <input></label>
        <button>Entrar</button>
        <p data-test="state"></p>
        <script>
          document.querySelector('button').onclick = () => {
            const user = document.querySelector('input').value;
            document.querySelector('[data-test=state]').textContent =
              user === 'student' ? 'Autenticado' : 'Bloqueado';
          };
        </script>
        """);

    page.getByLabel("Usuário").fill(usuario);
    page.getByRole(AriaRole.BUTTON,
        new Page.GetByRoleOptions().setName("Entrar")).click();

    assertThat(page.getByTestId("state")).hasText(estadoEsperado);
  }

  static Stream<Arguments> casosDeLogin() {
    return Stream.of(
        Arguments.of("student", "Autenticado"),
        Arguments.of("blocked", "Bloqueado"));
  }

  private static BigDecimal preco(String texto) {
    return new BigDecimal(texto.replace("$", "").trim());
  }
}
