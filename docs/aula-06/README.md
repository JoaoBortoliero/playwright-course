# Aula 6 — Interações avançadas e eventos

Tempo sugerido: 4–5 horas. Pré-requisito: Aula 5 concluída. Esta aula usa
somente o laboratório local.

## Objetivos

Automatizar upload, download, diálogos, popup, iframe e teclado usando eventos
determinísticos, sem sleeps e sem serviços externos.

## 1. Espere o evento, não o relógio

Algumas ações criam um objeto novo. Registre a espera antes de disparar a ação:

```java
Download download = page.waitForDownload(() -> page.getByText("Baixar").click());
Page popup = page.waitForPopup(() -> page.getByRole(BUTTON, ...).click());
```

Isso elimina a corrida “clicar e só depois começar a observar”. Em Selenium,
trocar janelas exige handles e polling; no Playwright, o popup é outra `Page`
do mesmo contexto e nasce do evento observado.

Leia a assinatura em quatro partes: o dono é `Page`; a entrada é um callback;
o callback dispara a ação; o retorno é o objeto criado pelo evento. O
Playwright registra o observador antes de executar o callback.

## 2. Arquivos e diálogos

`setInputFiles(path)` controla diretamente o `input[type=file]`; não automatize
a janela nativa do sistema. Para downloads, valide o nome sugerido e o conteúdo
salvo em diretório temporário.

### Upload completo

```java
Path arquivo = Files.createTempFile("curso-playwright-", ".txt");
Files.writeString(arquivo, "conteúdo de teste");
page.getByLabel("Arquivo").setInputFiles(arquivo);
assertThat(page.getByTestId("file-name"))
    .hasText(arquivo.getFileName().toString());
Files.deleteIfExists(arquivo);
```

`setInputFiles` injeta um `Path` no controle HTML. Ele não abre uma janela do
Windows, por isso funciona igualmente em headless e no Jenkins.

### Download completo

```java
Download download = page.waitForDownload(() ->
    page.getByRole(AriaRole.LINK,
        new Page.GetByRoleOptions().setName("Baixar relatório")).click());

assertEquals("report.txt", download.suggestedFilename());
Path destino = Files.createTempFile("relatorio-", ".txt");
download.saveAs(destino);
assertEquals("relatório determinístico\n", Files.readString(destino));
```

O download pertence ao contexto. `saveAs` fornece um arquivo estável para a
assertion e para eventual evidência.

Diálogos bloqueiam a página. Registre `page.onceDialog(dialog -> ...)` antes do
clique. Aceite ou rejeite conscientemente e valide a mensagem.

```java
AtomicReference<String> mensagem = new AtomicReference<>();
page.onceDialog(dialog -> {
  mensagem.set(dialog.message());
  dialog.accept();
});
page.getByRole(AriaRole.BUTTON,
    new Page.GetByRoleOptions().setName("Abrir diálogo")).click();
assertEquals("Confirmação do laboratório", mensagem.get());
```

`onceDialog` trata apenas o próximo diálogo. `AtomicReference` guarda o valor
recebido dentro do callback para que seja verificado depois.

## 3. Frames e teclado

Um iframe tem DOM próprio. Use `frameLocator("iframe[title=...]")` e continue
com locators semânticos dentro dele. Não atravesse o DOM com XPath.

Use `press("Enter")` para teclas e `keyboard()` para sequências de baixo nível.
`fill()` é preferível a digitar caractere por caractere quando o objetivo é
preencher um campo.

### Iframe

```java
FrameLocator frame = page.frameLocator("iframe[title='Área incorporada']");
Locator confirmar = frame.getByRole(
    AriaRole.BUTTON,
    new FrameLocator.GetByRoleOptions().setName("Confirmar"));
confirmar.click();
assertThat(frame.getByRole(
    AriaRole.BUTTON,
    new FrameLocator.GetByRoleOptions().setName("Concluído"))).isVisible();
```

`FrameLocator` muda o escopo para o documento incorporado. Não é uma troca
global de janela e não exige XPath atravessando dois documentos.

Repare que não reutilizamos `confirmar` na assertion: o clique muda o nome
acessível do botão. Como um `Locator` reavalia sua consulta, procurar novamente
por nome “Confirmar” não encontraria o controle agora chamado “Concluído”.

### Teclado e conteúdo atrasado

```java
page.getByLabel("Atalho").press("Enter");
assertThat(page.getByTestId("key")).hasText("Enter recebido");

page.getByRole(AriaRole.BUTTON,
    new Page.GetByRoleOptions().setName("Carregar")).click();
assertThat(page.getByTestId("delayed")).hasText("Conteúdo pronto");
```

A última assertion tenta novamente; o teste não precisa conhecer o atraso.

## 4. O laboratório fornecido

`CourseLabServer` inicia em `127.0.0.1` com uma porta livre e encerra ao fim da
classe. Nenhuma chamada sai da máquina.

`CourseLabExtension` é infraestrutura pronta que controla somente esse
servidor e injeta `CourseLabServer` em hooks/testes:

```java
@ExtendWith(CourseLabExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class Exemplo {
  @BeforeEach
  void abrirPagina(CourseLabServer lab) {
    page.navigate(lab.baseUrl() + "/interactions");
  }
}
```

Ela não controla Playwright. O lifecycle do browser continua sendo o manual da
Aula 3. `@PlaywrightTest` só será ensinado na Aula 7.

## 5. Demonstração e exercício

```powershell
.\course.ps1 demo 06
```

Implemente `InteracoesAvancadasExercicioTest`:

1. crie arquivo temporário e faça upload;
2. aguarde download, salve e valide conteúdo;
3. valide e aceite o alerta;
4. capture popup e verifique seu título/status;
5. encontre o botão dentro do iframe e confirme;
6. envie Enter e valide a consequência;
7. clique em “Carregar” e aguarde o conteúdo atrasado com web-first assertion.

Implemente um teste por tipo de interação. Isso mantém a falha localizada e
permite executar apenas upload, download, diálogo, popup, iframe ou teclado.

### Desafio independente

Rejeite um diálogo em uma página criada com `setContent` e prove que a ação foi
cancelada. Depois explique quando `waitForEvent` genérico é preferível aos
métodos especializados.

## Validação e rubrica

```powershell
.\course.ps1 validate 06
```

- [ ] Observadores são registrados antes das ações.
- [ ] O teste não automatiza janelas nativas.
- [ ] O iframe é tratado pelo seu próprio escopo.
- [ ] Arquivos temporários são isolados e descartáveis.
- [ ] Não existem waits fixos.

Perguntas: por que o popup compartilha cookies? Por que `setInputFiles` é mais
estável que Robot? Qual recurso é dono do download?

Erros comuns: `NoSuchFileException` indica arquivo apagado cedo; timeout do
popup/download costuma indicar observador registrado depois do clique; locator
ausente no iframe costuma indicar busca feita na `Page`, não no `FrameLocator`.

Leituras: [downloads](https://playwright.dev/java/docs/downloads),
[upload](https://playwright.dev/java/docs/input#upload-files),
[frames](https://playwright.dev/java/docs/frames) e
[pages](https://playwright.dev/java/docs/pages).

Solução após a autoavaliação: `.\course.ps1 solution 06`.
