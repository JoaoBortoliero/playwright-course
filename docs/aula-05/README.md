# Aula 5 — Carrinho, checkout e jornadas de negócio

Tempo sugerido: 5–6 horas. Pré-requisito: Aula 4.

## Objetivos

Modelar uma jornada sem criar um teste monolítico, preparar dados de forma
legível, verificar estado em cada transição e calcular valores monetários.

## 1. Uma jornada é uma sequência de contratos

O teste de compra não deve apenas clicar até “Complete”. Observe os contratos:

```text
inventário -> produto adicionado e badge = 1
carrinho   -> item, quantidade e preço corretos
checkout   -> dados obrigatórios aceitos
resumo     -> subtotal + imposto = total
conclusão  -> confirmação visível
```

Verificações intermediárias localizam a origem da falha. Entretanto, não
transforme um teste em dezenas de detalhes cosméticos. Valide o estado que
protege a regra de negócio.

No Selenium é comum uma cadeia de `click()` seguida de uma verificação final.
O Playwright facilita assertions web-first em cada fronteira, mas a escolha do
oráculo continua sendo responsabilidade do testador.

## 2. Independência e dados

Cada teste inicia com contexto novo e constrói seu próprio estado. Não use
`@Order`, não reutilize carrinho de outro caso e não dependa de um “teste de
login”. Métodos auxiliares podem reduzir ruído nesta etapa; Page Objects entram
na Aula 7.

Represente os dados do comprador com um record:

```java
record Customer(String firstName, String lastName, String postalCode) {}
```

Não coloque credenciais reais no código. As credenciais do SauceDemo são
públicas e didáticas; Jenkins Credentials será usado para segredos reais.

## 3. Valores monetários

Extraia `Item total: $29.99`, mantenha apenas a parte numérica e converta para
`BigDecimal`. Calcule `subtotal.add(tax)` e compare com o total. Prefira
`compareTo` ou normalize a escala, pois `29.9` e `29.90` têm valores iguais e
escalas diferentes.

### Conversão e comparação, passo a passo

O SauceDemo apresenta rótulos como `Item total: $39.98`. O objeto observado é
texto; a regra de negócio usa números:

```java
private static BigDecimal dinheiro(String rotulo) {
  String numero = rotulo.replaceAll("[^0-9.]", "");
  return new BigDecimal(numero);
}
```

```java
BigDecimal subtotal = dinheiro(page.getByTestId("subtotal-label").textContent());
BigDecimal imposto = dinheiro(page.getByTestId("tax-label").textContent());
BigDecimal total = dinheiro(page.getByTestId("total-label").textContent());

assertEquals(0, subtotal.add(imposto).compareTo(total));
```

`compareTo` devolve zero quando os valores numéricos são iguais, mesmo se as
escalas forem diferentes. Não compare textos e não use `double` para dinheiro.

## 4. Como escolher um produto pelo domínio

Existem vários botões “Add to cart”. Comece pelo item que contém o nome e só
depois procure o botão dentro dele:

```java
Locator item = page.getByTestId("inventory-item")
    .filter(new Locator.FilterOptions().setHasText("Sauce Labs Backpack"));

item.getByRole(
    AriaRole.BUTTON,
    new Locator.GetByRoleOptions().setName("Add to cart"))
    .click();
```

Esse padrão não depende da posição do produto. Transforme-o em um método
auxiliar privado nesta aula; Page Objects só serão introduzidos na Aula 7.

## 5. Mapa da jornada de checkout

| Estado ou ação | Test ID |
|---|---|
| link do carrinho | `shopping-cart-link` |
| badge | `shopping-cart-badge` |
| itens do carrinho | `inventory-item` |
| iniciar checkout | `checkout` |
| primeiro nome | `firstName` |
| sobrenome | `lastName` |
| CEP | `postalCode` |
| continuar | `continue` |
| subtotal | `subtotal-label` |
| imposto | `tax-label` |
| total | `total-label` |
| finalizar | `finish` |
| confirmação | `complete-header` |

Exemplo de uma transição observável:

```java
adicionarProduto("Sauce Labs Backpack");
assertThat(page.getByTestId("shopping-cart-badge")).hasText("1");
page.getByTestId("shopping-cart-link").click();
assertThat(page.getByTestId("inventory-item")).hasCount(1);
```

O clique é uma ação; badge e conteúdo são oráculos. Sem assertions
intermediárias, uma falha ao adicionar apareceria apenas no fim do checkout.

## 6. Dados do comprador e testes negativos

Um `record` agrupa valores que viajam juntos:

```java
record Customer(String firstName, String lastName, String postalCode) {}

Customer customer = new Customer("Ada", "Lovelace", "01000-000");
page.getByTestId("firstName").fill(customer.firstName());
page.getByTestId("lastName").fill(customer.lastName());
page.getByTestId("postalCode").fill(customer.postalCode());
```

Para obrigatoriedade, cada caso pode fornecer os valores e a mensagem:

```java
static Stream<Arguments> camposObrigatorios() {
  return Stream.of(
      Arguments.of("", "Lovelace", "01000-000", "First Name is required"),
      Arguments.of("Ada", "", "01000-000", "Last Name is required"),
      Arguments.of("Ada", "Lovelace", "", "Postal Code is required"));
}
```

O corpo preenche os três dados, clica em `continue` e verifica o test ID
`error`. Como mecânica e tipo de resultado são iguais, não é necessário `if`.

## 7. Demonstração e exercício guiado

Execute a demonstração e implemente `CheckoutExercicioTest`:

```powershell
.\course.ps1 demo 05
```

Antes de programar, abra `CheckoutEDinheiroDemonstracao.java` e identifique
onde o produto é limitado pelo nome, qual assertion comprova a transição e por
que valores monetários deixam de ser texto.

1. login do usuário padrão;
2. adicione Backpack e Bike Light pelo escopo do item;
3. confira badge `2` e conteúdo do carrinho;
4. remova um item e confira badge e ausência;
5. preencha checkout com um objeto de dados;
6. valide subtotal, imposto e total;
7. conclua e verifique `Thank you for your order!`.

Crie também testes negativos independentes para first name, last name e postal
code obrigatórios. Use parametrização somente se o nome do campo e a mensagem
esperada permanecerem claros.

Implemente em checkpoints: login e badge `2`; carrinho com dois itens; remoção
e badge `1`; identificação; cálculo do resumo; conclusão; três variações
negativas. Execute `exercise 05` depois de cada checkpoint.

### Desafio independente

Prove que voltar do carrinho ao catálogo preserva o badge dentro do mesmo
contexto e que um novo teste começa com carrinho vazio. Explique por que isso
não é contradição: persistência de jornada e isolamento de teste têm escopos
diferentes.

## Validação e rubrica

```powershell
.\course.ps1 validate 05
```

- [ ] Há assertions nas transições relevantes.
- [ ] Produtos são escolhidos pelo domínio, não por posição.
- [ ] Dinheiro usa `BigDecimal` e total é recalculado.
- [ ] Cenários negativos não dependem do positivo.
- [ ] Falhar no carrinho produz diagnóstico diferente de falhar na conclusão.

Perguntas: qual é o menor conjunto de cenários do checkout? Quando uma jornada
E2E deve ser dividida? O que pertence ao dado e o que pertence à ação?

Erros comuns: `strict mode violation` indica botão sem escopo; total divergente
geralmente indica texto não convertido ou item diferente; badge ausente depois
de remover o último item é comportamento válido, não texto `0`.

Leituras: [isolamento](https://playwright.dev/java/docs/browser-contexts) e
[boas práticas](https://playwright.dev/java/docs/best-practices).

Solução após a autoavaliação: `.\course.ps1 solution 05`.
