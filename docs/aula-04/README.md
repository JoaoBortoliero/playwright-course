# Aula 4 — Estratégia de testes, catálogo e dados

Tempo sugerido: 5–7 horas. Pré-requisito: Aula 3 concluída.

Nesta aula nenhuma pesquisa externa é necessária para concluir os exercícios
principais. O desafio independente indica separadamente o que pode ser
pesquisado.

## Objetivos

Ao terminar, você deverá conseguir:

- reutilizar conscientemente a fixture manual da Aula 3;
- distinguir um `Locator` vivo de uma lista capturada naquele instante;
- validar quantidade e conteúdo de uma coleção;
- selecionar uma opção de um elemento `<select>`;
- converter preços da interface para `BigDecimal`;
- verificar ordenação sem modificar a evidência observada;
- criar um teste parametrizado com `@MethodSource`;
- decidir quando parametrização melhora ou prejudica a leitura.

## 1. O que muda em relação à Aula 3

Na Aula 3 você construiu a infraestrutura:

```text
uma vez por classe: Playwright -> Browser
para cada teste:    BrowserContext -> Page
```

Na Aula 4 essa infraestrutura continua igual. A novidade está na forma de
observar coleções e representar dados. Não use ainda `@PlaywrightTest`: essa
extensão será ensinada na Aula 7.

O exercício deve começar com a mesma estrutura de campos e hooks da Aula 3:

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
}
```

Isso não é uma nova arquitetura: é aplicação direta do conhecimento da Aula 3.
Cada invocação de um teste parametrizado também recebe seu próprio
`@BeforeEach` e `@AfterEach`, portanto começa em um contexto limpo.

## 2. Transformando requisitos em verificações

Considere a regra “o catálogo deve apresentar todos os produtos”. Uma
verificação fraca seria testar apenas se apareceu algum texto. Uma verificação
mais útil separa o contrato em observações:

- existem exatamente seis itens;
- cada item possui um nome não vazio;
- o conjunto de nomes corresponde ao catálogo esperado;
- a posição não faz parte do contrato enquanto nenhuma ordenação foi escolhida.

Os nomes públicos esperados atualmente no exercício são:

```text
Sauce Labs Backpack
Sauce Labs Bike Light
Sauce Labs Bolt T-Shirt
Sauce Labs Fleece Jacket
Sauce Labs Onesie
Test.allTheThings() T-Shirt (Red)
```

Mantenha esses dados em uma coleção nomeada. Não espalhe seis assertions
independentes nem presuma que Backpack precisa estar na posição zero.

Partições de teste representam comportamentos diferentes. No login:

- `standard_user`: autenticação aceita;
- `locked_out_user`: autenticação recusada por bloqueio;
- campos vazios: validação obrigatória;
- credenciais desconhecidas: autenticação recusada.

Parametrização é adequada quando a mecânica permanece igual e os dados
descrevem claramente o resultado. Se o corpo começar a acumular `if`, jornadas
ou assertions completamente diferentes, prefira testes separados.

## 3. Mapa do catálogo do SauceDemo

O SauceDemo oferece contratos `data-test`. Como o atributo já foi configurado
na fixture, estes locators ficam disponíveis:

| Elemento | Locator |
|---|---|
| conjunto de itens | `page.getByTestId("inventory-item")` |
| nomes | `page.getByTestId("inventory-item-name")` |
| descrições | `page.getByTestId("inventory-item-desc")` |
| preços | `page.getByTestId("inventory-item-price")` |
| seletor de ordenação | `page.getByTestId("product-sort-container")` |

Os valores das opções de ordenação são:

| Regra visível | `value` usado por `selectOption` |
|---|---|
| Name (A to Z) | `az` |
| Name (Z to A) | `za` |
| Price (low to high) | `lohi` |
| Price (high to low) | `hilo` |

Exemplo de ação:

```java
page.getByTestId("product-sort-container").selectOption("lohi");
```

`selectOption` atua no `<select>` como um usuário escolhendo aquela opção. No
SauceDemo a lista é reordenada imediatamente. Em uma aplicação assíncrona,
seria necessário aguardar uma consequência observável da ordenação, não usar
`Thread.sleep`.

## 4. Locator vivo e fotografia da coleção

Isto ainda é uma consulta que o Playwright resolverá quando for utilizada:

```java
Locator nomesDosProdutos = page.getByTestId("inventory-item-name");
```

Esta assertion possui retry até encontrar seis elementos ou atingir o timeout:

```java
assertThat(nomesDosProdutos).hasCount(6);
```

Já esta operação captura os textos existentes naquele momento:

```java
List<String> nomesExibidos = nomesDosProdutos.allTextContents();
```

Agora `nomesExibidos` é uma lista Java comum. Se o DOM mudar depois, seu
conteúdo não muda automaticamente. Isso é desejável quando queremos preservar
a evidência que será comparada.

Para comparar sem depender da ordem, transforme as coleções em conjuntos:

```java
Set<String> esperado = Set.of("Alpha", "Beta", "Gamma");
Set<String> exibido = Set.copyOf(nomesExibidos);

assertEquals(esperado, exibido);
```

Esse exemplo usa nomes fictícios. No exercício, monte o conjunto com os seis
nomes informados na seção anterior.

## 5. De texto da tela para BigDecimal

O navegador entrega preços como texto:

```text
$29.99
$9.99
$15.99
```

Para verificar ordenação numérica, primeiro remova o símbolo usado pelo
SauceDemo e converta o restante:

```java
private static BigDecimal converterPreco(String texto) {
  String numero = texto.replace("$", "").trim();
  return new BigDecimal(numero);
}
```

Use `BigDecimal` porque valores monetários não devem depender da aproximação
binária de `double`. Neste site o separador decimal é sempre ponto. Em um
sistema internacionalizado, a conversão precisaria considerar a moeda e o
locale em vez de apenas remover `$`.

Uma lista de textos pode ser transformada com Stream:

```java
List<BigDecimal> precosExibidos = page
    .getByTestId("inventory-item-price")
    .allTextContents()
    .stream()
    .map(CatalogoEOrdenacaoExercicioTest::converterPreco)
    .toList();
```

A forma equivalente com laço, caso seja mais clara, é:

```java
List<BigDecimal> precosExibidos = new ArrayList<>();

for (String texto : page.getByTestId("inventory-item-price").allTextContents()) {
  precosExibidos.add(converterPreco(texto));
}
```

Ambas são válidas. O objetivo da aula é o teste, não obrigar o uso de Stream.

## 6. Como provar que a lista está ordenada

Depois de selecionar `lohi`, teremos uma lista na ordem exibida:

```java
List<BigDecimal> exibidos = obterPrecosDaTela();
```

Crie uma cópia e ordene somente a cópia:

```java
List<BigDecimal> esperados = new ArrayList<>(exibidos);
esperados.sort(Comparator.naturalOrder());

assertEquals(esperados, exibidos);
```

O raciocínio é:

```text
exibidos  = evidência produzida pela aplicação
esperados = mesma evidência submetida à regra correta de ordenação
```

Não faça isto:

```java
exibidos.sort(Comparator.naturalOrder());
assertEquals(exibidos, exibidos);
```

Além de poder falhar porque listas produzidas por `Stream.toList()` não são
modificáveis, o teste destruiria a evidência original e compararia uma lista
com ela mesma. Ficaria verde mesmo se a aplicação tivesse exibido a ordem
errada.

## 7. Parametrização com MethodSource

Um teste parametrizado é executado uma vez para cada argumento fornecido:

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

O método indicado por `@MethodSource` fornece os dados:

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

Imports necessários:

```java
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.stream.Stream;
```

Não existe `if` no teste: cada linha de dados informa o estado observável que
deve aparecer. Essa técnica é útil aqui para aprender `MethodSource`. Em uma
suíte de produção, se os fluxos positivo e bloqueado passarem a exigir passos
ou diagnósticos diferentes, separe-os novamente em testes com nomes próprios.

## 8. Demonstração executável

Leia antes de executar:

`src/test/java/br/com/curso/playwright/aula04/CatalogoEParametrizacaoDemonstracao.java`

A demonstração é local e mostra:

- a fixture manual reaproveitada da Aula 3;
- `selectOption`;
- captura de uma coleção;
- conversão para `BigDecimal`;
- cópia e ordenação da evidência;
- `@ParameterizedTest` com `@MethodSource` sem condicionais.

Execute:

```powershell
.\course.ps1 demo 04
```

Antes de continuar, responda:

1. Qual variável contém a evidência original?
2. Qual variável pode ser ordenada sem criar falso positivo?
3. Quantas vezes o teste parametrizado é executado?
4. O `@BeforeEach` roda uma vez para o método ou uma vez para cada argumento?

## 9. Exercício guiado

Implemente:

`src/test/java/br/com/curso/playwright/aula04/CatalogoEOrdenacaoExercicioTest.java`

Não apague o que você já escreveu. Complete uma parte de cada vez.

### Parte A — fixture e login

1. mantenha a fixture manual da Aula 3;
2. confirme `@TestInstance(PER_CLASS)`;
3. em cada teste, navegue para `/` e faça login;
4. não crie Page Object nem use `@PlaywrightTest` ainda.

Critério intermediário: o login chega a `Products` em um contexto novo.

### Parte B — catálogo completo

No método `deveExibirCatalogoCompleto`:

1. obtenha o locator `inventory-item-name`;
2. use `hasCount(6)`;
3. capture os nomes com `allTextContents()`;
4. compare o conjunto observado com os seis nomes da seção 2;
5. não dependa da posição dos produtos.

Critério intermediário: alterar a ordem visual não faz esse teste falhar.

### Parte C — menor preço primeiro

No método `deveOrdenarProdutosPorMenorPreco`:

1. escolha `lohi` em `product-sort-container`;
2. capture os textos de `inventory-item-price`;
3. converta cada texto para `BigDecimal`;
4. preserve a lista exibida;
5. crie e ordene uma cópia com `Comparator.naturalOrder()`;
6. compare cópia esperada e evidência exibida.

Critério intermediário: se você inverter propositalmente para `hilo`, a
assertion crescente deve falhar.

### Parte D — dados de login

Adicione o teste parametrizado apresentado na seção 7 usando
`@ParameterizedTest` e `@MethodSource`. O objetivo é executar a mesma mecânica
para `standard_user` e `locked_out_user`, com o estado esperado descrito nos
argumentos e sem `if` no corpo.

Critério intermediário: o relatório mostra duas invocações com nomes legíveis,
e cada uma recebe um contexto novo.

Execute durante o desenvolvimento:

```powershell
.\course.ps1 exercise 04
.\course.ps1 exercise 04 -Headed
```

Quando todas as partes estiverem implementadas:

```powershell
.\course.ps1 validate 04
```

## 10. Desafio independente

Somente depois do exercício principal:

- valide a ordenação Z–A usando `za` e `Comparator.reverseOrder()`;
- prove que todos os produtos têm nome e descrição não vazios, preço positivo
  e botão de compra;
- mantenha usuários que introduzem defeitos conhecidos fora da regressão
  principal e registre a exploração no diário.

`Locator.evaluateAll()` pode ser pesquisado como alternativa, mas não é
necessário nem será cobrado nesta aula. Prefira as APIs de locator ensinadas.

## 11. Erros comuns e como interpretá-los

- `Cannot resolve symbol ParameterizedTest`: falta o import de
  `org.junit.jupiter.params.ParameterizedTest`.
- `Could not find factory method`: o texto de `@MethodSource` não corresponde
  ao nome do método ou o método não é `static` nesta fixture.
- `UnsupportedOperationException` ao ordenar: você tentou alterar diretamente
  uma lista não modificável; crie `new ArrayList<>(lista)`.
- comparação de preços estranha: os valores ainda são `String` ou ainda contêm
  `$`; converta para `BigDecimal`.
- estado vazando entre argumentos: o contexto não está sendo recriado e fechado
  em `@BeforeEach`/`@AfterEach`.
- `strict mode violation`: o locator representa vários itens e você tentou uma
  ação que exige apenas um; restrinja pelo domínio antes de clicar.

## 12. Rubrica e reflexão

- [ ] A fixture mantém Playwright/Browser por classe e Context/Page por teste.
- [ ] O catálogo é validado sem índices fixos.
- [ ] A coleção é capturada conscientemente com `allTextContents()`.
- [ ] Dinheiro usa `BigDecimal`, nunca `double`.
- [ ] A evidência original não é ordenada nem sobrescrita.
- [ ] O teste parametrizado não possui `if` para escolher o oráculo.
- [ ] Não existem esperas fixas, XPath estrutural ou `force=true`.
- [ ] Cada teste e cada invocação parametrizada funcionam isoladamente.

Perguntas finais:

1. Quando uma parametrização piora a leitura?
2. Por que ordenar a própria lista extraída cria um falso positivo?
3. Por que `allTextContents()` é uma fotografia e `Locator` não é?
4. O que mudaria na conversão monetária se o preço fosse `R$ 29,99`?
5. Qual usuário público do SauceDemo você manteria apenas em exploração?

Consulte o gabarito somente depois de uma tentativa registrada:

```powershell
.\course.ps1 solution 04
```

Leituras oficiais opcionais para consolidação:
[JUnit parametrizado](https://docs.junit.org/current/user-guide/#writing-tests-parameterized-tests),
[locators](https://playwright.dev/java/docs/locators) e
[assertions](https://playwright.dev/java/docs/test-assertions).
