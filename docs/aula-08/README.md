# Aula 8 — API, autenticação e estado reutilizável

Tempo sugerido: 4–5 horas. Pré-requisito: Aula 7 concluída. O SauceDemo não
expõe uma API pública de negócio; os exercícios usam o laboratório local.

## Objetivos

Usar `APIRequestContext`, validar contratos HTTP, preparar estado por API e
entender quando reutilizar `storageState` sem destruir o isolamento.

## 1. Teste de API não é UI sem browser

Uma resposta possui status, headers e corpo. Valide o contrato relevante:

```java
APIResponse response = request.get("/api/items");
assertEquals(200, response.status());
assertTrue(response.ok());
String body = response.text();
```

Desserialize ou analise JSON quando a estrutura importa. Não valide o corpo
inteiro como string se a ordem dos campos não for contrato. Sempre descarte
respostas (`response.dispose()`) quando necessário para liberar memória.

### Criando o cliente

```java
try (Playwright playwright = Playwright.create()) {
  APIRequestContext request = playwright.request().newContext(
      new APIRequest.NewContextOptions().setBaseURL(lab.baseUrl()));
  try {
    // chamadas HTTP
  } finally {
    request.dispose();
  }
}
```

`APIRequestContext` é o cliente HTTP. `setBaseURL` permite usar caminhos como
`/api/items`. Ele não cria `Browser`, `BrowserContext` nem `Page`.

### GET e ownership da resposta

```java
APIResponse response = request.get("/api/items");
try {
  assertEquals(200, response.status());
  assertTrue(response.ok());
  assertTrue(response.headers().get("content-type").contains("application/json"));
  String body = response.text();
  assertTrue(body.contains("Backpack"));
  assertTrue(body.contains("Bike Light"));
} finally {
  response.dispose();
}
```

Cada `APIResponse` retém seu corpo até ser descartada. Feche respostas antes do
contexto HTTP.

### POST com corpo JSON

```java
APIResponse response = request.post(
    "/api/items",
    RequestOptions.create().setData(Map.of("name", "Novo item")));
try {
  assertEquals(201, response.status());
  assertTrue(response.text().contains("\"id\":3"));
  assertTrue(response.text().contains("\"name\":\"Novo item\""));
} finally {
  response.dispose();
}
```

Importe `com.microsoft.playwright.options.RequestOptions` e `java.util.Map`.
Em uma API real, use uma biblioteca JSON quando estrutura e tipos forem parte
ampla do contrato; o laboratório é propositalmente pequeno.

No Selenium, uma biblioteca HTTP adicional costuma ser necessária. Playwright
oferece `APIRequestContext` e pode compartilhar cookies com um
`BrowserContext`, permitindo preparação e verificação híbridas.

## 2. Estado autenticado

`BrowserContext.storageState()` serializa cookies e local storage. Um arquivo
de estado é sensível: pode equivaler a uma sessão ativa. Gere-o em runtime,
armazene-o apenas em `artifacts`/diretório temporário, nunca faça commit e use
conta apropriada ao ambiente.

Reutilizar autenticação reduz custo, mas não autoriza compartilhar contexto.
Cada teste cria contexto novo carregando a mesma fotografia inicial. Testes que
alteram permissões ou logout devem construir seu próprio estado.

### Criando e reutilizando o estado

```java
BrowserContext origem = browser.newContext(
    new Browser.NewContextOptions().setBaseURL(lab.baseUrl()));
Page page = origem.newPage();
page.navigate("/auth");
page.getByRole(AriaRole.BUTTON,
    new Page.GetByRoleOptions().setName("Entrar")).click();
assertThat(page.getByTestId("session")).hasText("Autenticado");

Path stateFile = Files.createTempFile("playwright-state-", ".json");
origem.storageState(
    new BrowserContext.StorageStateOptions().setPath(stateFile));
origem.close();

BrowserContext autenticado = browser.newContext(
    new Browser.NewContextOptions()
        .setBaseURL(lab.baseUrl())
        .setStorageStatePath(stateFile));
```

O novo contexto não é o antigo: ele apenas começa com a fotografia de cookies
e local storage. Prove a autorização com a API ligada ao contexto:

```java
APIResponse profile = autenticado.request().get("/api/profile");
try {
  assertEquals(200, profile.status());
  assertTrue(profile.text().contains("student"));
} finally {
  profile.dispose();
  autenticado.close();
  Files.deleteIfExists(stateFile);
}
```

Um contexto criado sem `setStorageStatePath` deve receber 401. Esse teste
negativo prova que a autorização veio do estado carregado.

## 3. Preparação por API

Use API para pré-condições quando a UI de preparação não é o objeto do teste.
Não prepare por uma API privada instável sem contrato com a equipe. Mantenha ao
menos uma cobertura UI do login real.

Não confunda:

| Estratégia | O que prova |
|---|---|
| login pela UI | formulário e integração visual de autenticação |
| preparação por API | pré-condição rápida quando o foco do teste é outro |
| `storageState` | fotografia inicial reutilizada em contextos novos |

## 4. Exercício

```powershell
.\course.ps1 demo 08
```

Implemente `ApiEStorageStateExercicioTest`:

1. GET `/api/items`: status 200, content type JSON e dois itens;
2. POST `/api/items`: status 201, id e nome esperados;
3. GET `/api/error`: status 503 e erro de manutenção;
4. autentique pela UI local, salve `storageState` em arquivo temporário;
5. crie contexto novo com esse estado e consulte `/api/profile`;
6. prove que contexto sem estado recebe 401.

Divida em checkpoints: GET; POST; erro 503; login local; arquivo de estado;
contexto autenticado; contexto anônimo. Feche cada `APIResponse` em `finally`.

Antes do exercício, identifique na demonstração quem cria e fecha
`Playwright`, `APIRequestContext`, `Browser`, `BrowserContext`, respostas e
arquivo temporário.

### Desafio independente

Crie uma função de preparação por API que retorne apenas o identificador
necessário ao teste. Explique como faria cleanup numa API real e o que ocorreria
se dois testes paralelos reutilizassem o mesmo registro.

## Validação e rubrica

```powershell
.\course.ps1 validate 08
```

- [ ] Status e conteúdo são verificados separadamente.
- [ ] Estado autenticado não entra no Git.
- [ ] Contextos continuam independentes.
- [ ] API é usada como pré-condição, não para evitar testar toda a UI.
- [ ] Recursos HTTP e arquivos temporários têm ownership claro.

Perguntas: quando storage state fica obsoleto? Que segredo ele pode conter? Em
que caso preparar dados pela UI é a escolha correta?

Erros comuns: 401 no contexto autenticado costuma indicar estado salvo antes
do login; arquivo preso no Windows indica contexto ainda aberto; URL inválida
indica caminho relativo enviado a um cliente sem `baseURL`.

Leituras: [API testing](https://playwright.dev/java/docs/api-testing),
[autenticação](https://playwright.dev/java/docs/auth) e
[APIRequestContext](https://playwright.dev/java/docs/api/class-apirequestcontext).

Solução após a autoavaliação: `.\course.ps1 solution 08`.
