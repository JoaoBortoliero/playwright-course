# Aula 9 - Rede, eventos e mocking controlado

Tempo sugerido: 4 a 5 horas. Pré-requisito: Aula 8 concluída. Esta aula usa o laboratório local para manter as respostas determinísticas.

## Objetivos

Observar requests e responses, sincronizar por resposta, simular contratos com `route/fulfill`, abortar tráfego e capturar erros de console e página.

## 1. Observar antes de alterar

Listeners ajudam no diagnóstico:

```java
page.onRequest(request -> log(request.method(), request.url()));
page.onResponse(response -> log(response.status(), response.url()));
```

Um listener observa todos os eventos futuros e não bloqueia o teste. Guarde somente os dados imutáveis necessários ao diagnóstico:

```java
List<String> requests = new CopyOnWriteArrayList<>();
page.onRequest(request ->
        requests.add(request.method() + " " + request.url()));
```

Use uma coleção thread-safe porque callbacks podem ocorrer assincronamente.

Para sincronização, registre `waitForResponse` antes do clique e filtre por URL e método. A espera de rede prova o transporte; valide também a consequência na UI. `networkidle` não representa a conclusão de uma regra de negócio e é uma heurística inadequada para aplicações que mantêm conexões abertas.

### Esperando uma resposta específica

```java
Response response = page.waitForResponse(
        candidate -> candidate.url().endsWith("/api/items")
                && candidate.request().method().equals("GET"),
        () -> page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Carregar itens"))
                .click());

assertEquals(200, response.status());
assertThat(page.getByTestId("result")).containsText("Backpack");
```

O primeiro oráculo prova o transporte; o segundo prova que a UI utilizou a resposta. Filtrar apenas pelo status 200 seria amplo demais.

No Selenium, esse controle costuma exigir CDP, proxy ou biblioteca adicional e variar por browser. Playwright trata rede como parte do contexto e da página. Isso simplifica a mecânica, mas não torna mock equivalente à integração real.

## 2. Mocking com intenção

`page.route("**/api/items", route -> route.fulfill(...))` permite testar estado vazio, erro ou payload raro. Um mock valida a reação do frontend ao contrato, não a integração real. Mantenha testes reais separados e nomeie claramente os cenários simulados.

Use `fulfill()` para responder com dados controlados e `abort()` para simular falha de transporte. Quando uma rota genérica interceptar tráfego que não deve ser alterado, utilize `resume()` ou `fallback()`, conforme a estratégia de roteamento.

Registre as rotas antes da navegação ou ação e remova-as quando o escopo terminar.

### Fulfill: resposta criada pelo teste

```java
page.route("**/api/items", route ->
        route.fulfill(new Route.FulfillOptions()
                .setStatus(200)
                .setContentType("application/json")
                .setBody("[]")));
```

A página receberá `[]` sem chamar o servidor. O nome do teste deve deixar explícito que se trata de mock.

### Abort: falha de transporte

```java
page.route("**/api/blocked", route -> route.abort());
page.getByRole(
        AriaRole.BUTTON,
        new Page.GetByRoleOptions().setName("Requisição bloqueável"))
        .click();
assertThat(page.getByTestId("result")).hasText("blocked:erro");
```

`abort()` simula ausência de resposta. Um status 503 é diferente: existe transporte e o servidor respondeu com erro.

## 3. Console e page errors

`page.onConsoleMessage` e `page.onPageError` coletam problemas do cliente. Não falhe indiscriminadamente por qualquer warning de terceiro. Defina severidade e propriedade do erro. Evidência sem política vira ruído.

```java
List<String> errors = new CopyOnWriteArrayList<>();
page.onPageError(errors::add);
page.setContent("<button onclick=\"setTimeout(() => { throw new Error('falha controlada') })\">Executar</button>");
page.getByText("Executar").click();
```

Depois, aguarde uma consequência observável e verifique se `errors` contém a mensagem. Como o callback é assíncrono, sincronize pela condição observada em vez de usar espera fixa.

Quando usado após uma navegação, `setContent()` substitui o conteúdo atual da página. No exercício, esse trecho cria um cenário JavaScript controlado e não depende do laboratório.

## 4. Exercício

Execute a demonstração:

```powershell
.\course.ps1 demo 09
```

Por padrão, o teste utiliza o Microsoft Edge instalado e executa com o navegador visível. Para executar sem exibir o navegador:

```powershell
.\course.ps1 demo 09 -Headless
```

Implemente `RedeEMockingExercicioTest`:

1. em `deveObservarRespostaReal`, registre os requests, espere a resposta GET `/api/items` e valide método, URL, status e consequência na UI;
2. em `deveCumprirContratoComMock`, intercepte `/api/items`, responda uma lista vazia ou alternativa e valide a consequência na UI;
3. em `deveTratarFalhaDeRede`, aborte `/api/blocked`, valide `blocked:erro` e capture um erro JavaScript controlado em uma página criada com `setContent()`.

Faça primeiro o teste real e deixe-o verde. Só então crie outro teste com mock. Não registre a rota mockada no hook comum, pois isso transformaria silenciosamente todos os testes de integração em testes simulados.

### Desafio independente

Altere uma resposta real com `route.fetch()` e `fulfill()`, preservando os headers e acrescentando um item. Explique por que isso é mais frágil que um fixture inteiramente controlado.

## Validação e rubrica

```powershell
.\course.ps1 validate 09
```

- [ ] A rota é registrada antes do request.
- [ ] O filtro de resposta considera URL e método.
- [ ] Testes mockados deixam isso explícito no nome ou na tag.
- [ ] Há pelo menos uma integração não mockada.
- [ ] A consequência na UI também é validada.
- [ ] A captura do page error utiliza sincronização sem espera fixa.

Perguntas: o que um mock não consegue provar? Quando abortar é melhor que responder 503? Por que `networkidle` não é um oráculo de negócio?

Erros comuns: mock sem efeito indica rota registrada tarde ou glob incorreto; timeout no `waitForResponse` indica predicate diferente do request real; UI correta sem status observado não prova qual resposta produziu o estado.

## Leituras

- [Network](https://playwright.dev/java/docs/network)
- [Mock APIs](https://playwright.dev/java/docs/mock)
- [Events](https://playwright.dev/java/docs/events)

Solução após a autoavaliação:

```powershell
.\course.ps1 solution 09
```
