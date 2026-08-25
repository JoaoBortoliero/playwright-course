# Aula 9 — Rede, eventos e mocking controlado

Tempo sugerido: 4–5 horas. Pré-requisito: Aula 8 concluída. Esta aula usa o
laboratório local para manter as respostas determinísticas.

## Objetivos

Observar requests/responses, sincronizar por resposta, simular contratos com
`route/fulfill`, abortar tráfego e capturar erros de console/página.

## 1. Observar antes de alterar

Listeners ajudam no diagnóstico:

```java
page.onRequest(request -> log(request.method(), request.url()));
page.onResponse(response -> log(response.status(), response.url()));
```

Um listener observa todos os eventos futuros e não bloqueia o teste. Guarde
somente dados imutáveis necessários ao diagnóstico:

```java
List<String> requests = new CopyOnWriteArrayList<>();
page.onRequest(request ->
    requests.add(request.method() + " " + request.url()));
```

Use uma coleção thread-safe porque callbacks podem ocorrer assincronamente.

Para sincronização, registre `waitForResponse` antes do clique e filtre por URL
e método. A espera de rede prova transporte; valide também a consequência na
UI. `networkidle` não representa conclusão de negócio e é uma heurística ruim
para aplicações que mantêm conexões abertas.

### Esperando uma resposta específica

```java
Response response = page.waitForResponse(
    candidate -> candidate.url().endsWith("/api/items")
        && candidate.request().method().equals("GET"),
    () -> page.getByRole(AriaRole.BUTTON,
        new Page.GetByRoleOptions().setName("Carregar itens")).click());

assertEquals(200, response.status());
assertThat(page.getByTestId("result")).containsText("Backpack");
```

O primeiro oráculo prova transporte; o segundo prova que a UI utilizou a
resposta. Filtrar apenas por status 200 seria amplo demais.

No Selenium, esse controle costuma exigir CDP, proxy ou biblioteca adicional e
variar por browser. Playwright trata rede como parte do contexto e da página;
isso simplifica a mecânica, mas não torna mock equivalente à integração real.

## 2. Mocking com intenção

`page.route("**/api/items", route -> route.fulfill(...))` permite testar estado
vazio, erro ou payload raro. Um mock valida a reação do frontend ao contrato,
não a integração real. Mantenha testes reais separados e nomeie claramente os
cenários simulados.

Use `route.resume()` para tráfego não alterado, `fulfill()` para responder e
`abort()` para falha de rede. Registre rotas antes da navegação/ação e remova-as
quando o escopo terminar.

### Fulfill: resposta criada pelo teste

```java
page.route("**/api/items", route ->
    route.fulfill(new Route.FulfillOptions()
        .setStatus(200)
        .setContentType("application/json")
        .setBody("[]")));
```

Registre antes da ação. A página receberá `[]` sem chamar o servidor. O nome do
teste deve deixar explícito que se trata de mock.

### Abort: falha de transporte

```java
page.route("**/api/blocked", route -> route.abort());
page.getByRole(AriaRole.BUTTON,
    new Page.GetByRoleOptions().setName("Requisição bloqueável")).click();
assertThat(page.getByTestId("result")).hasText("blocked:erro");
```

`abort` simula ausência de resposta. Um 503 é diferente: existe transporte e
o servidor respondeu com erro.

## 3. Console e page errors

`page.onConsoleMessage` e `page.onPageError` coletam problemas do cliente. Não
falhe indiscriminadamente por qualquer warning de terceiro; defina severidade
e propriedade do erro. Evidência sem política vira ruído.

```java
List<String> errors = new CopyOnWriteArrayList<>();
page.onPageError(errors::add);
page.setContent("<button onclick=\"setTimeout(() => { throw new Error('falha controlada') })\">Executar</button>");
page.getByText("Executar").click();
```

Depois aguarde uma consequência observável e verifique que `errors` contém a
mensagem. Em produção, não falhe por qualquer warning de terceiro sem política.

## 4. Exercício

```powershell
.\course.ps1 demo 09
```

Implemente `RedeEMockingExercicioTest`:

1. espere a resposta `/api/items` disparada pelo botão e valide status + UI;
2. intercepte `/api/items` e responda uma lista vazia/alternativa;
3. aborte `/api/blocked` e verifique `blocked:erro`;
4. registre requests e prove método/URL observados;
5. em página local criada com `setContent`, capture um erro JavaScript.

Faça primeiro o teste real e deixe-o verde. Só então crie outro teste com mock.
Não registre a rota mockada no hook comum: isso transformaria silenciosamente
todos os testes de integração em testes simulados.

### Desafio independente

Altere uma resposta real com `route.fetch()` e `fulfill()` preservando headers,
mas acrescentando um item. Explique por que isso é mais frágil que um fixture
inteiramente controlado.

## Validação e rubrica

```powershell
.\course.ps1 validate 09
```

- [ ] A rota é registrada antes do request.
- [ ] O filtro de resposta é específico.
- [ ] Testes mockados deixam isso explícito no nome/tag.
- [ ] Há pelo menos uma integração não mockada.
- [ ] A consequência na UI também é validada.

Perguntas: o que um mock não consegue provar? Quando abortar é melhor que
responder 503? Por que `networkidle` não é um oráculo de negócio?

Erros comuns: mock sem efeito indica rota registrada tarde ou glob incorreto;
timeout no `waitForResponse` indica predicate diferente do request real; UI
correta sem status observado não prova qual resposta produziu o estado.

Leituras: [network](https://playwright.dev/java/docs/network),
[mock APIs](https://playwright.dev/java/docs/mock) e
[events](https://playwright.dev/java/docs/events).

Solução após a autoavaliação: `.\course.ps1 solution 09`.
