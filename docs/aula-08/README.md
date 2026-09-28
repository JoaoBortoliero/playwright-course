# Aula 8 - API, autenticacao e estado reutilizavel

Tempo sugerido: 4 a 5 horas.  
Pre-requisito: Aula 7 concluida.

O SauceDemo nao expoe uma API publica de negocio. Por isso, os exercicios desta aula utilizam somente o laboratorio local.

## Objetivos

Ao terminar esta aula, voce devera conseguir:

- utilizar `APIRequestContext` para realizar chamadas HTTP;
- validar status, headers e corpo separadamente;
- criar recursos com requisicoes `POST`;
- tratar respostas de erro;
- preparar estado autenticado pela interface;
- salvar e reutilizar `storageState` em contextos novos;
- preservar o isolamento mesmo ao reutilizar autenticacao;
- fechar respostas, contextos HTTP e arquivos temporarios corretamente.

## 1. Teste de API nao é teste de UI sem navegador

Uma resposta HTTP possui status, headers e corpo. Valide somente as partes relevantes do contrato:

```java
APIResponse response = request.get("/api/items");

try {
  assertEquals(200, response.status());
  assertTrue(response.ok());
  assertTrue(response.headers()
      .get("content-type")
      .contains("application/json"));
} finally {
  response.dispose();
}
```

Quando a estrutura do JSON fizer parte do contrato, utilize uma biblioteca de serializacao ou analise apropriada. Evite comparar o corpo inteiro como texto quando a ordem dos campos nao for relevante.

Cada `APIResponse` deve ser descartada com `dispose()` depois de utilizada.

## 2. Criando o cliente HTTP

`APIRequestContext` e o cliente HTTP do Playwright:

```java
try (Playwright playwright = Playwright.create()) {
  APIRequestContext request = playwright.request().newContext(
      new APIRequest.NewContextOptions()
          .setBaseURL(lab.baseUrl()));

  try {
    // Chamadas HTTP
  } finally {
    request.dispose();
  }
}
```

`setBaseURL` permite utilizar caminhos relativos, como `/api/items`. A criacao de `APIRequestContext` nao inicia `Browser`, `BrowserContext` ou `Page`.

A ordem de fechamento deve ser:

```text
APIResponse -> APIRequestContext -> Playwright
```

## 3. GET, POST e resposta de erro

### GET

```java
APIResponse response = request.get("/api/items");

try {
  assertEquals(200, response.status());
  assertTrue(response.ok());
  assertTrue(response.text().contains("Backpack"));
  assertTrue(response.text().contains("Bike Light"));
} finally {
  response.dispose();
}
```

### POST

```java
APIResponse response = request.post(
    "/api/items",
    RequestOptions.create()
        .setData(Map.of("name", "Novo item")));

try {
  assertEquals(201, response.status());
  assertTrue(response.text().contains("\"id\":3"));
  assertTrue(response.text().contains("\"name\":\"Novo item\""));
} finally {
  response.dispose();
}
```

### Erro esperado

```java
APIResponse response = request.get("/api/error");

try {
  assertEquals(503, response.status());
  assertFalse(response.ok());
  assertTrue(response.text().contains("maintenance"));
} finally {
  response.dispose();
}
```

Status de erro esperado nao representa falha tecnica do teste. O teste deve validar o contrato da resposta recebida.

## 4. Estado autenticado

`BrowserContext.storageState()` serializa cookies e local storage. Esse arquivo pode representar uma sessao autenticada e deve ser tratado como informacao sensivel.

Regras importantes:

- gere o arquivo durante a execucao;
- armazene-o somente em diretorio temporario ou de artefatos;
- nao inclua o arquivo no Git;
- remova-o ao final do teste;
- nao compartilhe o mesmo `BrowserContext` entre testes.

Reutilizar `storageState` nao significa reutilizar o contexto. Cada teste deve criar um contexto novo a partir da mesma fotografia inicial.

## 5. Criando e reutilizando storageState

Primeiro, autentique pela interface e salve o estado:

```java
Path stateFile = Files.createTempFile(
    "playwright-state-", ".json");

BrowserContext origem = browser.newContext(
    new Browser.NewContextOptions()
        .setBaseURL(lab.baseUrl()));

try {
  Page page = origem.newPage();
  page.navigate("/auth");

  page.getByRole(
      AriaRole.BUTTON,
      new Page.GetByRoleOptions().setName("Entrar"))
      .click();

  assertThat(page.getByTestId("session"))
      .hasText("Autenticado");

  origem.storageState(
      new BrowserContext.StorageStateOptions()
          .setPath(stateFile));
} finally {
  origem.close();
}
```

Depois, carregue o estado em um contexto novo:

```java
BrowserContext autenticado = browser.newContext(
    new Browser.NewContextOptions()
        .setBaseURL(lab.baseUrl())
        .setStorageStatePath(stateFile));
```

Valide a autorizacao pela API associada ao contexto:

```java
APIResponse profile = autenticado.request()
    .get("/api/profile");

try {
  assertEquals(200, profile.status());
  assertTrue(profile.text().contains("student"));
} finally {
  profile.dispose();
  autenticado.close();
  Files.deleteIfExists(stateFile);
}
```

Um contexto criado sem `setStorageStatePath` deve receber `401`. Esse teste negativo prova que a autorizacao veio do estado carregado.

## 6. Preparacao por API

Utilize API para criar pre-condicoes quando a preparacao pela interface nao for o objeto do teste.

| Estrategia | O que prova |
|---|---|
| Login pela interface | Formulario e integracao visual de autenticacao |
| Preparacao por API | Pre-condicao rapida para outro objetivo de teste |
| `storageState` | Estado inicial reutilizado em contextos novos |

Nao utilize uma API privada e instavel sem contrato com a equipe. Mantenha pelo menos uma cobertura do login real pela interface.

## 7. Demonstracao executavel

Leia antes de executar:

`src/test/java/br/com/curso/playwright/aula08/ApiEStorageStateDemonstracao.java`

Antes da execucao, identifique quem cria e fecha:

- `Playwright`;
- `APIRequestContext`;
- `APIResponse`;
- `Browser`;
- `BrowserContext`;
- arquivo temporario de estado.

Execute com o Microsoft Edge visivel:

```powershell
.\course.ps1 demo 08
```

Execute sem exibir o navegador:

```powershell
.\course.ps1 demo 08 -Headless
```

A parte exclusivamente HTTP nao abre o navegador. O Edge e utilizado somente no teste que autentica pela interface e cria o `storageState`.

## 8. Exercicio guiado

Implemente:

`src/test/java/br/com/curso/playwright/aula08/ApiEStorageStateExercicioTest.java`

Remova `@Disabled` quando iniciar a implementacao.

Implemente em checkpoints:

1. `GET /api/items`: valide status `200`, content type JSON e os dois itens;
2. `POST /api/items`: valide status `201`, identificador e nome esperados;
3. `GET /api/error`: valide status `503` e mensagem de manutencao;
4. autentique pela interface local;
5. salve o `storageState` em arquivo temporario;
6. crie um contexto novo com o estado salvo;
7. consulte `/api/profile` e valide status `200`;
8. crie um contexto sem estado e valide status `401`.

Feche cada `APIResponse` em um bloco `finally`.

Execute durante o desenvolvimento com o Edge visivel:

```powershell
.\course.ps1 exercise 08
```

Execute sem exibir o navegador:

```powershell
.\course.ps1 exercise 08 -Headless
```

Quando concluir:

```powershell
.\course.ps1 validate 08
```

## 9. Desafio independente

Crie uma funcao de preparacao por API que retorne apenas o identificador necessario ao teste.

Depois, explique:

- como o recurso seria removido em uma API real;
- quem seria responsavel pelo cleanup;
- o que poderia ocorrer se testes paralelos reutilizassem o mesmo registro;
- como gerar dados unicos para evitar conflito.

## 10. Erros comuns

- `401` no contexto autenticado: o estado foi salvo antes da conclusao do login ou nao foi carregado no contexto novo;
- arquivo temporario preso no Windows: algum contexto ainda utiliza o arquivo ou nao foi fechado;
- URL invalida: um caminho relativo foi enviado a um cliente sem `baseURL`;
- consumo crescente de memoria: respostas nao foram descartadas com `dispose()`;
- estado vazando entre testes: o mesmo `BrowserContext` esta sendo compartilhado;
- execucao sempre visivel: o modo headless foi fixado no codigo em vez de ser obtido por `TestConfig` e `BrowserFactory`;
- estado autenticado no Git: o arquivo de sessao nao foi tratado como informacao sensivel.

## 11. Rubrica e reflexao

- [ ] Status, headers e conteudo sao verificados separadamente.
- [ ] Cada `APIResponse` e descartada.
- [ ] O `APIRequestContext` e fechado depois das respostas.
- [ ] O estado autenticado nao e incluido no Git.
- [ ] Cada teste continua utilizando um contexto independente.
- [ ] A API e utilizada como pre-condicao, nao para eliminar toda a cobertura pela interface.
- [ ] O arquivo temporario possui ownership e cleanup claros.
- [ ] O contexto anonimo recebe `401`.
- [ ] O contexto autenticado recebe `200`.

Perguntas finais:

1. Quando um arquivo de `storageState` fica obsoleto?
2. Quais informacoes sensiveis ele pode conter?
3. Quando preparar dados pela interface e a escolha correta?
4. Por que reutilizar estado nao significa reutilizar contexto?
5. Qual e a ordem correta de fechamento dos recursos HTTP?

Consulte a solucao somente depois da autoavaliacao:

```powershell
.\course.ps1 solution 08
```

## Leituras oficiais

- [Testes de API](https://playwright.dev/java/docs/api-testing)
- [Autenticacao](https://playwright.dev/java/docs/auth)
- [APIRequestContext](https://playwright.dev/java/docs/api/class-apirequestcontext)
