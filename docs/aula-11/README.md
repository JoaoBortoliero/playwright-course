# Aula 11 — Depuração, evidências e Allure

Tempo sugerido: 4–5 horas. Pré-requisito: Aula 10 concluída.

## Objetivos

Escolher a evidência certa, usar Inspector e Trace Viewer, capturar screenshot,
vídeo e trace em falhas e publicar resultados legíveis no Allure.

## 1. Diagnóstico antes da repetição

Uma falha útil responde: qual passo, estado, URL, console e request estavam
presentes? Reexecutar cegamente até ficar verde destrói a evidência de
flakiness. Use:

- headed/slow motion para observação inicial;
- `PWDEBUG=1` e Inspector para pausar e explorar locators;
- screenshot para estado visual final;
- trace para timeline, DOM, rede, console e fonte;
- vídeo para sequência geral, com menor detalhe técnico.

Codegen ajuda a descobrir elementos, mas seu resultado é rascunho. Refatore
locators e assertions para refletir o domínio.

### Inspector no Windows

```powershell
$env:PWDEBUG = "1"
.\course.ps1 exercise 11 -Headed
Remove-Item Env:PWDEBUG
```

O Inspector permite pausar, avançar ações e explorar locators. Remova a
variável depois; um Jenkins esperando interação manual nunca terminaria.

Em Selenium, screenshot, logs, HAR/proxy e vídeo frequentemente vêm de
integrações separadas. O trace Playwright correlaciona ações, DOM, rede e
console; screenshot simples ainda é melhor quando já responde à pergunta.

## 2. Política de evidência

Gravar tudo sempre consome disco e pode capturar dados sensíveis. O padrão é
trace e screenshot em falha, vídeo desligado localmente e retenção definida no
CI. Evidências ficam em `artifacts` e não entram no Git.

A extensão inicia trace antes do teste, salva-o somente quando necessário e
fecha o contexto no `finally`. O nome do arquivo é sanitizado para funcionar em
Windows e Linux.

As políticas aceitas são:

| Valor | Comportamento |
|---|---|
| `off` | não captura |
| `on-failure` | mantém somente quando o teste falha |
| `always` | mantém em sucesso e falha |

Passe propriedades sem alterar Java:

```powershell
.\mvn-local.ps1 test -Dtest=EvidenciasExercicioTest `
  -Dtrace=always -Dscreenshot=always -Dvideo=always
```

### Screenshot manual

```java
Path screenshot = config.artifactsDir().resolve("estado.png");
page.screenshot(new Page.ScreenshotOptions()
    .setPath(screenshot)
    .setFullPage(true));
assertTrue(Files.size(screenshot) > 0);
```

### Trace manual: o que a extensão faz

```java
context.tracing().start(new Tracing.StartOptions()
    .setScreenshots(true)
    .setSnapshots(true)
    .setSources(true));

// ações e assertions

context.tracing().stop(new Tracing.StopOptions()
    .setPath(Path.of("artifacts", "trace.zip")));
```

O trace pertence ao `BrowserContext`. Sempre pare o trace antes de fechar o
contexto. Na extensão, essa sequência está em `afterEach` e considera se o
teste falhou.

### Vídeo

Vídeo precisa ser configurado na criação do contexto:

```java
Browser.NewContextOptions options = new Browser.NewContextOptions()
    .setRecordVideoDir(Path.of("artifacts", "videos"));
```

O arquivo é finalizado quando o contexto fecha. Por isso não é possível decidir
“começar a gravar” somente depois da falha; a política decide se o arquivo já
gravado será mantido ou removido.

Abra um trace:

```powershell
.\mvn-local.ps1 exec:java '-Dexec.mainClass=com.microsoft.playwright.CLI' '-Dexec.args=show-trace artifacts/seu-trace.zip'
```

## 3. Allure

O adapter `allure-jupiter` registra casos e anexos em
`target/allure-results`. Steps devem representar ações relevantes, não cada
linha. Nunca anexe senha, token, cookie ou payload pessoal sem mascaramento.

O relatório é uma visão; XML JUnit continua sendo a fonte do status no Jenkins.

```java
Allure.step("Adicionar produto ao carrinho", () -> {
  inventory.addProduct("Sauce Labs Backpack");
});

Allure.addAttachment(
    "Ambiente",
    "text/plain",
    "browser=" + config.browser());
```

Um step deve representar intenção de negócio. Não transforme cada `fill` e
`click` em step; o trace já possui esse nível técnico.

## 4. Exercício

```powershell
.\course.ps1 demo 11
```

Implemente `EvidenciasExercicioTest`:

1. execute com `-Dtrace=always -Dscreenshot=always`;
2. localize os arquivos produzidos;
3. abra o trace e encontre ação, locator, DOM e request;
4. crie steps Allure para preparar, agir e verificar;
5. anexe texto diagnóstico sem segredo;
6. provoque uma falha controlada, analise-a e depois restaure o teste verde.

Sequência recomendada: execute verde com `always`; localize PNG/ZIP/vídeo;
abra o trace; introduza uma assertion errada; confirme anexos de falha;
restaure; execute com `on-failure` e verifique que o sucesso não acumula trace.

### Desafio independente

Crie uma política de retenção: quais artefatos manter em smoke, regressão
aprovada e falha? Justifique custo, privacidade e tempo de diagnóstico.

## Validação e rubrica

```powershell
.\course.ps1 validate 11
```

- [ ] Evidências aparecem na falha e não escondem o erro original.
- [ ] Teardown ocorre mesmo se captura falhar.
- [ ] Anexos não contêm segredos.
- [ ] Steps são de negócio e não ruído.
- [ ] Você consegue explicar a causa usando o trace.

Perguntas: quando screenshot é insuficiente? Por que trace “always” pode ser
perigoso? Qual evidência você abriria primeiro numa falha intermitente?

Erros comuns: ZIP vazio indica trace parado após fechar o contexto; vídeo
ausente antes do fechamento é esperado; Allure sem casos geralmente indica
diretório diferente de `target/allure-results`; evidência com segredo é uma
falha de segurança mesmo que o teste esteja verde.

Leituras: [debug](https://playwright.dev/java/docs/debug),
[trace viewer](https://playwright.dev/java/docs/trace-viewer),
[videos](https://playwright.dev/java/docs/videos) e
[Allure JUnit 5](https://allurereport.org/docs/junit5/).

Solução após a autoavaliação: `.\course.ps1 solution 11`.
