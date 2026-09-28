# Aula 5 - Carrinho, checkout e jornadas de negocio

Tempo sugerido: 5 a 6 horas.  
Pre-requisito: Aula 4 concluida.

## Objetivos

Ao terminar esta aula, voce devera conseguir:

- modelar uma jornada sem criar um teste monolitico;
- preparar dados de forma legivel;
- verificar o estado em cada transicao relevante;
- selecionar produtos pelo dominio, sem depender da posicao;
- representar dados relacionados com `record`;
- calcular e comparar valores monetarios com `BigDecimal`;
- manter cenarios positivos e negativos independentes.

## 1. Uma jornada e uma sequencia de contratos

O teste de compra nao deve apenas clicar ate a conclusao. Cada etapa possui um estado observavel:

```text
inventario -> produto adicionado e badge atualizado
carrinho   -> item, quantidade e preco corretos
checkout   -> dados obrigatorios aceitos
resumo     -> subtotal + imposto = total
conclusao  -> confirmacao visivel
```

Assertions intermediarias ajudam a localizar a origem da falha. Entretanto, nao valide detalhes cosmeticos sem valor para a regra de negocio.

O Playwright oferece assertions web-first em cada fronteira, mas a escolha do que deve ser validado continua sendo responsabilidade do testador.

## 2. Independencia e dados

Cada teste recebe um novo `BrowserContext` e constroi o proprio estado. Portanto:

- nao utilize `@Order`;
- nao reutilize o carrinho criado por outro teste;
- nao dependa de um teste anterior para realizar login;
- nao compartilhe `Page` entre testes.

Metodos auxiliares privados podem reduzir repeticao nesta etapa. Page Objects serao introduzidos somente na Aula 7.

Represente os dados do comprador com um `record`:

```java
record Customer(String firstName, String lastName, String postalCode) { }
```

As credenciais do SauceDemo sao publicas e utilizadas somente para fins didaticos. Credenciais reais nao devem ser armazenadas no codigo.

## 3. Valores monetarios

O SauceDemo apresenta valores como:

```text
Item total: $39.98
Tax: $3.20
Total: $43.18
```

Converta os textos para `BigDecimal`:

```java
private static BigDecimal dinheiro(String rotulo) {
  String numero = rotulo.replaceAll("[^0-9.]", "");
  return new BigDecimal(numero);
}
```

Depois, calcule o total esperado:

```java
BigDecimal subtotal = dinheiro(
    page.getByTestId("subtotal-label").textContent());
BigDecimal imposto = dinheiro(
    page.getByTestId("tax-label").textContent());
BigDecimal total = dinheiro(
    page.getByTestId("total-label").textContent());

assertEquals(0, subtotal.add(imposto).compareTo(total));
```

Utilize `compareTo` porque valores como `29.9` e `29.90` sao numericamente iguais, embora possuam escalas diferentes. Nao compare os textos e nao utilize `double` para valores monetarios.

## 4. Como escolher um produto pelo dominio

Existem varios botoes chamados `Add to cart`. Primeiro localize o item pelo nome e depois procure o botao dentro dele:

```java
Locator item = page.getByTestId("inventory-item")
    .filter(new Locator.FilterOptions()
        .setHasText("Sauce Labs Backpack"));

item.getByRole(
    AriaRole.BUTTON,
    new Locator.GetByRoleOptions().setName("Add to cart"))
    .click();
```

Esse locator nao depende da posicao do produto. Nesta aula, ele pode ser encapsulado em um metodo auxiliar privado.

## 5. Mapa da jornada de checkout

| Estado ou acao | Test ID |
|---|---|
| Link do carrinho | `shopping-cart-link` |
| Badge do carrinho | `shopping-cart-badge` |
| Itens do carrinho | `inventory-item` |
| Iniciar checkout | `checkout` |
| Primeiro nome | `firstName` |
| Sobrenome | `lastName` |
| CEP | `postalCode` |
| Continuar | `continue` |
| Subtotal | `subtotal-label` |
| Imposto | `tax-label` |
| Total | `total-label` |
| Finalizar | `finish` |
| Confirmacao | `complete-header` |

Exemplo de transicao observavel:

```java
adicionarProduto("Sauce Labs Backpack");
assertThat(page.getByTestId("shopping-cart-badge")).hasText("1");

page.getByTestId("shopping-cart-link").click();
assertThat(page.getByTestId("inventory-item")).hasCount(1);
```

O clique representa a acao. O badge e o conteudo do carrinho representam evidencias do resultado.

## 6. Dados do comprador e testes negativos

Um `record` agrupa valores relacionados:

```java
Customer customer = new Customer("Ada", "Lovelace", "01000-000");

page.getByTestId("firstName").fill(customer.firstName());
page.getByTestId("lastName").fill(customer.lastName());
page.getByTestId("postalCode").fill(customer.postalCode());
```

Os campos obrigatorios podem ser validados com um teste parametrizado:

```java
static Stream<Arguments> camposObrigatorios() {
  return Stream.of(
      Arguments.of("", "Lovelace", "01000-000", "First Name is required"),
      Arguments.of("Ada", "", "01000-000", "Last Name is required"),
      Arguments.of("Ada", "Lovelace", "", "Postal Code is required")
  );
}
```

O corpo do teste deve preencher os tres campos, clicar em `continue` e validar o test ID `error`. Como a mecanica e o tipo de resultado permanecem iguais, nao e necessario utilizar condicionais.

## 7. Demonstracao executavel

Leia antes de executar:

`src/test/java/br/com/curso/playwright/aula05/CheckoutEDinheiroDemonstracao.java`

Identifique:

- como o produto e localizado pelo nome;
- qual assertion comprova a transicao;
- por que valores monetarios sao convertidos para `BigDecimal`;
- como a configuracao de execucao e obtida por `TestConfig` e `BrowserFactory`.

Execute com o Microsoft Edge visivel:

```powershell
.\course.ps1 demo 05
```

Execute sem exibir o navegador:

```powershell
.\course.ps1 demo 05 -Headless
```

## 8. Exercicio guiado

Implemente:

`src/test/java/br/com/curso/playwright/aula05/CheckoutExercicioTest.java`

Remova `@Disabled` quando iniciar a implementacao.

### Jornada positiva

Implemente os seguintes checkpoints:

1. realize login com `standard_user`;
2. adicione `Sauce Labs Backpack` e `Sauce Labs Bike Light`;
3. valide o badge com valor `2`;
4. abra o carrinho e confirme os dois produtos;
5. remova um produto;
6. valide o badge com valor `1` e a ausencia do produto removido;
7. inicie o checkout;
8. preencha os dados com um objeto `Customer`;
9. valide subtotal, imposto e total;
10. finalize a compra;
11. valide a mensagem `Thank you for your order!`.

### Cenarios negativos

Crie variacoes independentes para:

- primeiro nome obrigatorio;
- sobrenome obrigatorio;
- CEP obrigatorio.

Utilize parametrizacao porque a mecanica e o tipo de resultado sao iguais. Cada invocacao deve preparar seu proprio carrinho e checkout.

Execute durante o desenvolvimento com o Edge visivel:

```powershell
.\course.ps1 exercise 05
```

Para executar sem exibir o navegador:

```powershell
.\course.ps1 exercise 05 -Headless
```

Quando a implementacao estiver concluida:

```powershell
.\course.ps1 validate 05
```

## 9. Desafio independente

Prove que:

- voltar do carrinho ao catalogo preserva o badge dentro do mesmo contexto;
- um novo teste comeca com o carrinho vazio.

Isso nao representa contradicao. A persistencia da jornada ocorre dentro do mesmo contexto, enquanto o isolamento cria um contexto novo para cada teste.

## 10. Erros comuns

- `strict mode violation`: o botao foi localizado sem limitar o escopo ao produto;
- total divergente: algum valor nao foi convertido corretamente ou o carrinho possui itens diferentes dos esperados;
- badge ausente depois de remover o ultimo item: esse e o comportamento esperado do SauceDemo, nao deve ser validado como texto `0`;
- estado vazando entre testes: o contexto nao esta sendo recriado ou fechado corretamente;
- teste negativo dependente do positivo: cada invocacao deve preparar seu proprio estado;
- execucao sempre visivel: a demonstracao fixou `.setHeadless(false)` em vez de utilizar `TestConfig` e `BrowserFactory`.

## 11. Rubrica e reflexao

- [ ] Existem assertions nas transicoes relevantes.
- [ ] Os produtos sao escolhidos pelo dominio, nao pela posicao.
- [ ] Valores monetarios utilizam `BigDecimal`.
- [ ] O total e recalculado pelo teste.
- [ ] Os cenarios negativos nao dependem da jornada positiva.
- [ ] Cada teste e cada invocacao parametrizada recebem um contexto novo.
- [ ] Nao existem esperas fixas, `force=true` ou dependencia de ordem.
- [ ] Uma falha no carrinho apresenta diagnostico diferente de uma falha na conclusao.

Perguntas finais:

1. Qual e o menor conjunto de cenarios necessario para cobrir o checkout?
2. Quando uma jornada E2E deve ser dividida?
3. O que pertence aos dados e o que pertence as acoes?
4. Quais assertions intermediarias realmente protegem regras de negocio?
5. Por que um novo contexto deve iniciar com o carrinho vazio?

Consulte a solucao somente depois da autoavaliacao:

```powershell
.\course.ps1 solution 05
```

## Leituras oficiais

- [Isolamento por BrowserContext](https://playwright.dev/java/docs/browser-contexts)
- [Boas praticas do Playwright](https://playwright.dev/java/docs/best-practices)
