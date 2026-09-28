# Aula 4 - Estrategia de testes, catalogo e dados

Tempo sugerido: 5 a 7 horas.  
Pre-requisito: Aula 3 concluida.

Nesta aula, nenhuma pesquisa externa e necessaria para concluir os exercicios principais. O desafio independente indica separadamente o que pode ser pesquisado.

## Objetivos

Ao terminar esta aula, voce devera conseguir:

- reutilizar conscientemente a fixture manual da Aula 3;
- distinguir um `Locator` vivo de uma lista capturada em um instante;
- validar quantidade e conteudo de uma colecao;
- selecionar uma opcao de um elemento `<select>`;
- converter precos da interface para `BigDecimal`;
- verificar ordenacao sem modificar a evidencia observada;
- criar um teste parametrizado com `@MethodSource`;
- decidir quando a parametrizacao melhora ou prejudica a leitura.

## 1. O que muda em relacao a Aula 3

Na Aula 3, voce construiu a infraestrutura:

```text
Uma vez por classe: Playwright -> Browser
Para cada teste:    BrowserContext -> Page
```

Na Aula 4, essa infraestrutura continua igual. A novidade esta na forma de observar colecoes e representar dados. Nao utilize `@PlaywrightTest` ainda, pois essa extensao sera ensinada na Aula 7.

O exercicio utiliza a mesma estrutura de campos e hooks da Aula 3:

```java
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CatalogoEOrdenacaoExercicioTest {
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
    context = browser.newContext(
        new Browser.NewContextOptions().setBaseURL(config.baseUrl()));
    context.setDefaultTimeout(config.timeoutMs());
    context.setDefaultNavigationTimeout(config.timeoutMs());
    page = context.newPage();
  }

  @AfterEach
  void fecharContexto() {
    try {
      if (context != null) {
        context.close();
      }
    } finally {
      context = null;
      page = null;
    }
  }

  @AfterAll
  void fecharBrowser() {
    try {
      if (browser != null) {
        browser.close();
      }
    } finally {
      browser = null;

      if (playwright != null) {
        playwright.close();
        playwright = null;
      }
    }
  }
}
```

Isso nao representa uma nova arquitetura. E a aplicacao direta do conhecimento da Aula 3. Cada invocacao de um teste parametrizado tambem recebe seu proprio `@BeforeEach` e `@AfterEach`, portanto comeca em um contexto limpo.

O Microsoft Edge instalado na maquina continua sendo o navegador utilizado. Por padrao, a execucao e visivel. Para executar sem exibir o navegador, utilize `-Headless`.

## 2. Transformando requisitos em verificacoes

Considere a regra: "o catalogo deve apresentar todos os produtos". Uma verificacao fraca testaria apenas se algum texto apareceu. Uma verificacao mais util separa o contrato em observacoes:

- existem exatamente seis itens;
- cada item possui nome nao vazio;
- o conjunto de nomes corresponde ao catalogo esperado;
- a posicao nao faz parte do contrato enquanto nenhuma ordenacao for escolhida.

Os nomes esperados no exercicio sao:

```text
Sauce Labs Backpack
Sauce Labs Bike Light
Sauce Labs Bolt T-Shirt
Sauce Labs Fleece Jacket
Sauce Labs Onesie
Test.allTheThings() T-Shirt (Red)
```

Mantenha esses dados em uma colecao nomeada. Nao espalhe seis assertions independentes nem presuma que `Sauce Labs Backpack` deve estar na primeira posicao.

Particoes de teste representam comportamentos diferentes. No login:

- `standard_user`: autenticacao aceita;
- `locked_out_user`: autenticacao recusada por bloqueio;
- campos vazios: validacao obrigatoria;
- credenciais desconhecidas: autenticacao recusada.

A parametrizacao e adequada quando a mecanica permanece igual e os dados descrevem claramente o resultado. Se o corpo comecar a acumular condicionais, jornadas ou assertions diferentes, prefira testes separados.

## 3. Mapa do catalogo do SauceDemo

O SauceDemo oferece contratos `data-test`. Como o atributo ja foi configurado na fixture, podem ser utilizados os seguintes test IDs:

| Elemento | Test ID |
|---|---|
| Conjunto de itens | `inventory-item` |
| Nomes | `inventory-item-name` |
| Descricoes | `inventory-item-desc` |
| Precos | `inventory-item-price` |
| Seletor de ordenacao | `product-sort-container` |

Os valores das opcoes de ordenacao sao:

| Regra visivel | Valor |
|---|---|
| Name (A to Z) | `az` |
| Name (Z to A) | `za` |
| Price (low to high) | `lohi` |
| Price (high to low) | `hilo` |

Exemplo:

```java
page.getByTestId("product-sort-container").selectOption("lohi");
```

No SauceDemo, a lista e reordenada imediatamente. Em uma aplicacao assincrona, deve-se aguardar uma consequencia observavel da ordenacao, sem utilizar `Thread.sleep`.

## 4. Locator vivo e fotografia da colecao

Um `Locator` representa uma consulta que sera resolvida quando for utilizada:

```java
Locator nomesDosProdutos = page.getByTestId("inventory-item-name");
```

A assertion abaixo possui retry ate encontrar seis elementos ou atingir o timeout:

```java
assertThat(nomesDosProdutos).hasCount(6);
```

A operacao abaixo captura os textos existentes naquele momento:

```java
List<String> nomesExibidos = nomesDosProdutos.allTextContents();
```

`nomesExibidos` passa a ser uma lista Java comum. Se o DOM mudar depois, seu conteudo nao sera atualizado automaticamente.

Para comparar sem depender da ordem, transforme as colecoes em conjuntos:

```java
Set<String> esperado = Set.of("Alpha", "Beta", "Gamma");
Set<String> exibido = Set.copyOf(nomesExibidos);

assertEquals(esperado, exibido);
```

No exercicio, utilize os seis nomes informados anteriormente.

## 5. Convertendo o texto da tela para BigDecimal

O navegador entrega os precos como texto:

```text
$29.99
$9.99
$15.99
```

Remova o simbolo monetario e converta o valor:

```java
private static BigDecimal converterPreco(String texto) {
  String numero = texto.replace("$", "").trim();
  return new BigDecimal(numero);
}
```

Utilize `BigDecimal` porque valores monetarios nao devem depender da aproximacao binaria de `double`. No SauceDemo, o separador decimal e sempre ponto. Em sistemas internacionalizados, a conversao deve considerar moeda e locale.

Exemplo com Stream:

```java
List<BigDecimal> precosExibidos = page
    .getByTestId("inventory-item-price")
    .allTextContents()
    .stream()
    .map(CatalogoEOrdenacaoExercicioTest::converterPreco)
    .toList();
```

A forma equivalente com laco tambem e valida. O objetivo da aula e o teste, nao impor o uso de Stream.

## 6. Como provar que a lista esta ordenada

Depois de selecionar `lohi`, capture a ordem exibida:

```java
List<BigDecimal> exibidos = obterPrecosDaTela();
```

Crie uma copia e ordene somente a copia:

```java
List<BigDecimal> esperados = new ArrayList<>(exibidos);
esperados.sort(Comparator.naturalOrder());

assertEquals(esperados, exibidos);
```

O raciocinio e:

```text
exibidos  = evidencia produzida pela aplicacao
esperados = copia da evidencia submetida a regra correta de ordenacao
```

Nao ordene a propria lista observada e depois a compare com ela mesma. Isso destruiria a evidencia original e criaria um falso positivo.

## 7. Parametrizacao com MethodSource

Um teste parametrizado e executado uma vez para cada argumento fornecido:

```java
@ParameterizedTest(name = "login de {0}")
@MethodSource("casosDeLogin")
void deveObservarResultadoDoLogin(
    String usuario,
    String testIdEsperado,
    String textoEsperado) {

  page.navigate("/");
  page.getByPlaceholder("Username").fill(usuario);
  page.getByPlaceholder("Password").fill("secret_sauce");
  page.getByRole(
      AriaRole.BUTTON,
      new Page.GetByRoleOptions().setName("Login"))
      .click();

  assertThat(page.getByTestId(testIdEsperado)).hasText(textoEsperado);
}
```

O metodo indicado por `@MethodSource` fornece os dados:

```java
static Stream<Arguments> casosDeLogin() {
  return Stream.of(
      Arguments.of("standard_user", "title", "Products"),
      Arguments.of(
          "locked_out_user",
          "error",
          "Epic sadface: Sorry, this user has been locked out.")
  );
}
```

Imports necessarios:

```java
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
```

Nao existe `if` no teste. Cada linha de dados informa o estado observavel esperado. Se os fluxos passarem a exigir passos ou diagnosticos diferentes, devem ser separados em testes com nomes proprios.

## 8. Demonstracao executavel

Leia antes de executar:

`src/test/java/br/com/curso/playwright/aula04/CatalogoEParametrizacaoDemonstracao.java`

A demonstracao local apresenta:

- a fixture manual reaproveitada da Aula 3;
- `selectOption`;
- captura de uma colecao;
- conversao para `BigDecimal`;
- copia e ordenacao da evidencia;
- `@ParameterizedTest` com `@MethodSource` sem condicionais.

Execute com o Edge visivel:

```powershell
.\course.ps1 demo 04
```

Execute sem exibir o Edge:

```powershell
.\course.ps1 demo 04 -Headless
```

Antes de continuar, responda:

1. Qual variavel contem a evidencia original?
2. Qual variavel pode ser ordenada sem criar falso positivo?
3. Quantas vezes o teste parametrizado e executado?
4. O `@BeforeEach` executa uma vez para o metodo ou uma vez para cada argumento?

## 9. Exercicio guiado

Implemente:

`src/test/java/br/com/curso/playwright/aula04/CatalogoEOrdenacaoExercicioTest.java`

Remova `@Disabled` quando iniciar a implementacao. Complete uma parte de cada vez.

### Parte A - Fixture e login

1. Mantenha a fixture manual da Aula 3.
2. Confirme o uso de `@TestInstance(PER_CLASS)`.
3. Em cada teste, navegue para `/` e realize o login.
4. Nao crie Page Object nem utilize `@PlaywrightTest`.

Criterio intermediario: o login chega a `Products` em um contexto novo.

### Parte B - Catalogo completo

No metodo `deveExibirCatalogoCompleto`:

1. obtenha o locator `inventory-item-name`;
2. utilize `hasCount(6)`;
3. capture os nomes com `allTextContents()`;
4. compare o conjunto observado com os seis nomes esperados;
5. nao dependa da posicao dos produtos.

Criterio intermediario: alterar a ordem visual nao faz esse teste falhar.

### Parte C - Menor preco primeiro

No metodo `deveOrdenarProdutosPorMenorPreco`:

1. escolha `lohi` em `product-sort-container`;
2. capture os textos de `inventory-item-price`;
3. converta cada texto para `BigDecimal`;
4. preserve a lista exibida;
5. crie e ordene uma copia com `Comparator.naturalOrder()`;
6. compare a copia esperada com a evidencia exibida.

Criterio intermediario: se a opcao for alterada propositalmente para `hilo`, a assertion crescente deve falhar.

### Parte D - Dados de login

Implemente o teste parametrizado apresentado na secao 7 com `@ParameterizedTest` e `@MethodSource`. A mesma mecanica deve ser executada para `standard_user` e `locked_out_user`, sem condicionais no corpo do teste.

Criterio intermediario: o relatorio apresenta duas invocacoes com nomes legiveis, e cada uma recebe um contexto novo.

Execute durante o desenvolvimento com o Edge visivel:

```powershell
.\course.ps1 exercise 04
```

Para executar sem exibir o navegador:

```powershell
.\course.ps1 exercise 04 -Headless
```

Quando todas as partes estiverem implementadas:

```powershell
.\course.ps1 validate 04
```

## 10. Desafio independente

Somente depois do exercicio principal:

- valide a ordenacao Z-A usando `za` e `Comparator.reverseOrder()`;
- prove que todos os produtos possuem nome e descricao nao vazios, preco positivo e botao de compra;
- mantenha usuarios que introduzem defeitos conhecidos fora da regressao principal e registre a exploracao no diario.

`Locator.evaluateAll()` pode ser pesquisado como alternativa, mas nao e necessario nem sera cobrado nesta aula. Prefira as APIs de locator ja ensinadas.

## 11. Erros comuns e como interpreta-los

- `Cannot resolve symbol ParameterizedTest`: falta o import de `org.junit.jupiter.params.ParameterizedTest`;
- `Could not find factory method`: o valor de `@MethodSource` nao corresponde ao nome do metodo, ou o metodo nao e compativel com o lifecycle utilizado;
- `UnsupportedOperationException` ao ordenar: a lista pode ser nao modificavel; crie `new ArrayList<>(lista)`;
- comparacao de precos incorreta: os valores continuam como `String` ou ainda possuem `$`; converta-os para `BigDecimal`;
- estado vazando entre argumentos: o contexto nao esta sendo recriado e fechado em `@BeforeEach` e `@AfterEach`;
- `strict mode violation`: o locator representa varios elementos e foi utilizado em uma acao que exige apenas um; restrinja-o pelo dominio.

> Como a classe utiliza `@TestInstance(PER_CLASS)`, o metodo de `@MethodSource` pode ser estatico, como no exemplo, mas nao precisa obrigatoriamente ser estatico.

## 12. Rubrica e reflexao

- [ ] A fixture mantem Playwright e Browser por classe, e Context e Page por teste.
- [ ] O catalogo e validado sem indices fixos.
- [ ] A colecao e capturada conscientemente com `allTextContents()`.
- [ ] Valores monetarios utilizam `BigDecimal`, nunca `double`.
- [ ] A evidencia original nao e ordenada nem sobrescrita.
- [ ] O teste parametrizado nao possui `if` para escolher o resultado esperado.
- [ ] Nao existem esperas fixas, XPath estrutural ou `force=true`.
- [ ] Cada teste e cada invocacao parametrizada funcionam isoladamente.

Perguntas finais:

1. Quando uma parametrizacao piora a leitura?
2. Por que ordenar a propria lista extraida cria um falso positivo?
3. Por que `allTextContents()` representa uma fotografia e `Locator` nao?
4. O que mudaria na conversao monetaria se o preco fosse `R$ 29,99`?
5. Qual usuario publico do SauceDemo deve ser mantido apenas em exploracao?

Consulte o gabarito somente depois de registrar uma tentativa:

```powershell
.\course.ps1 solution 04
```

## Leituras oficiais opcionais

- [Testes parametrizados no JUnit](https://docs.junit.org/current/user-guide/#writing-tests-parameterized-tests)
- [Locators no Playwright Java](https://playwright.dev/java/docs/locators)
- [Assertions no Playwright Java](https://playwright.dev/java/docs/test-assertions)
