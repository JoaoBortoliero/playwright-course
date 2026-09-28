# Aula 6 - Interacoes avancadas e eventos

Tempo sugerido: 4 a 5 horas.  
Pre-requisito: Aula 5 concluida.

Esta aula utiliza somente o laboratorio local. Nenhuma chamada externa e necessaria para executar os testes.

## Objetivos

Ao terminar esta aula, voce devera conseguir:

- automatizar upload e download de arquivos;
- tratar dialogos do navegador;
- capturar popups de forma deterministica;
- interagir com elementos dentro de iframe;
- utilizar teclado e teclas especiais;
- aguardar eventos sem esperas fixas;
- manter arquivos temporarios isolados e descartaveis.

## 1. Espere o evento, nao o relogio

Algumas acoes criam um novo objeto. A espera deve ser registrada antes de disparar a acao:

```java
Download download = page.waitForDownload(() ->
    page.getByText("Baixar").click());

Page popup = page.waitForPopup(() ->
    page.getByRole(AriaRole.BUTTON,
        new Page.GetByRoleOptions().setName("Abrir popup"))
        .click());
```

O callback registra o observador antes do clique. Isso evita a condicao de corrida em que o evento acontece antes de o teste comecar a observa-lo.

Um popup e outra `Page` pertencente ao mesmo `BrowserContext`. Por isso, ele compartilha cookies e demais estados do contexto que o originou.

## 2. Upload e download

### Upload

Utilize `setInputFiles` diretamente no `input[type=file]`. Nao automatize a janela nativa do sistema operacional.

```java
Path arquivo = Files.createTempFile("curso-playwright-", ".txt");

try {
  Files.writeString(arquivo, "conteudo de teste");

  page.getByLabel("Arquivo").setInputFiles(arquivo);

  assertThat(page.getByTestId("file-name"))
      .hasText(arquivo.getFileName().toString());
} finally {
  Files.deleteIfExists(arquivo);
}
```

Como `setInputFiles` atua diretamente no controle HTML, o comportamento e o mesmo em execucao visivel ou headless.

### Download

Registre `waitForDownload` antes do clique:

```java
Download download = page.waitForDownload(() ->
    page.getByRole(
        AriaRole.LINK,
        new Page.GetByRoleOptions().setName("Baixar relatorio"))
        .click());

assertEquals("report.txt", download.suggestedFilename());

Path destino = Files.createTempFile("relatorio-", ".txt");

try {
  download.saveAs(destino);
  assertEquals("relatorio deterministico\n", Files.readString(destino));
} finally {
  Files.deleteIfExists(destino);
}
```

O download pertence ao contexto. `saveAs` cria um arquivo estavel para validacao e eventual coleta como evidencia.

## 3. Dialogos

Dialogos JavaScript podem bloquear a pagina. Registre o tratamento antes da acao que abre o dialogo:

```java
AtomicReference<String> mensagem = new AtomicReference<>();

page.onceDialog(dialog -> {
  mensagem.set(dialog.message());
  dialog.accept();
});

page.getByRole(
    AriaRole.BUTTON,
    new Page.GetByRoleOptions().setName("Abrir dialogo"))
    .click();

assertEquals("Confirmacao do laboratorio", mensagem.get());
```

`onceDialog` trata somente o proximo dialogo. `AtomicReference` permite armazenar a mensagem recebida no callback e valida-la depois.

## 4. Iframes

Um iframe possui seu proprio documento. Utilize `FrameLocator` para mudar o escopo da busca:

```java
FrameLocator frame = page.frameLocator(
    "iframe[title='Area incorporada']");

frame.getByRole(
    AriaRole.BUTTON,
    new FrameLocator.GetByRoleOptions().setName("Confirmar"))
    .click();

assertThat(frame.getByRole(
    AriaRole.BUTTON,
    new FrameLocator.GetByRoleOptions().setName("Concluido")))
    .isVisible();
```

O clique altera o nome acessivel do botao. Por isso, a assertion cria uma nova consulta para localizar o estado `Concluido`.

Nao utilize XPath para atravessar o documento principal e o documento incorporado.

## 5. Teclado e conteudo atrasado

Utilize `press` para teclas especiais:

```java
page.getByLabel("Atalho").press("Enter");
assertThat(page.getByTestId("key")).hasText("Enter recebido");
```

Para conteudo que aparece com atraso, utilize uma assertion web-first:

```java
page.getByRole(
    AriaRole.BUTTON,
    new Page.GetByRoleOptions().setName("Carregar"))
    .click();

assertThat(page.getByTestId("delayed"))
    .hasText("Conteudo pronto");
```

A assertion tenta novamente ate encontrar o estado esperado ou atingir o timeout. Nao utilize `Thread.sleep` ou `waitForTimeout`.

## 6. Laboratorio local

`CourseLabServer` inicia em `127.0.0.1`, utiliza uma porta livre e e encerrado ao final da classe.

`CourseLabExtension` controla somente o servidor e injeta `CourseLabServer` nos hooks e testes:

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

A extensao nao controla o Playwright. O lifecycle do navegador continua seguindo a fixture manual apresentada na Aula 3.

O Microsoft Edge instalado na maquina continua sendo utilizado. Por padrao, a execucao e visivel. Para executar sem exibir o navegador, utilize `-Headless`.

## 7. Demonstracao executavel

Leia antes de executar:

`src/test/java/br/com/curso/playwright/aula06/InteracoesAvancadasDemonstracao.java`

Execute com o Edge visivel:

```powershell
.\course.ps1 demo 06
```

Execute sem exibir o navegador:

```powershell
.\course.ps1 demo 06 -Headless
```

Na demonstracao, identifique:

- onde cada observador e registrado antes da acao;
- como os arquivos temporarios sao removidos;
- como o popup e capturado;
- como o escopo muda para o iframe;
- onde a assertion web-first substitui uma espera fixa.

## 8. Exercicio guiado

Implemente:

`src/test/java/br/com/curso/playwright/aula06/InteracoesAvancadasExercicioTest.java`

Remova `@Disabled` quando iniciar a implementacao.

Crie um teste independente para cada grupo de interacao:

1. crie um arquivo temporario, realize o upload e valide o nome;
2. aguarde o download, valide o nome sugerido, salve o arquivo e valide o conteudo;
3. capture a mensagem do dialogo e aceite-o;
4. capture o popup e valide seu status;
5. localize e acione o botao dentro do iframe;
6. envie `Enter` e valide a consequencia;
7. clique em `Carregar` e aguarde o conteudo com assertion web-first.

Execute durante o desenvolvimento com o Edge visivel:

```powershell
.\course.ps1 exercise 06
```

Execute sem exibir o navegador:

```powershell
.\course.ps1 exercise 06 -Headless
```

Quando concluir:

```powershell
.\course.ps1 validate 06
```

## 9. Desafio independente

Crie uma pagina com `setContent`, abra um dialogo e utilize `dialog.dismiss()` para rejeita-lo. Depois, prove por uma evidencia observavel que a acao foi cancelada.

Explique tambem quando uma espera generica por evento seria preferivel aos metodos especializados, como `waitForDownload` e `waitForPopup`.

## 10. Erros comuns

- `NoSuchFileException`: o arquivo temporario foi removido antes de ser utilizado;
- timeout de popup ou download: o observador foi registrado depois da acao;
- locator nao encontrado no iframe: a busca foi feita na `Page`, e nao no `FrameLocator`;
- dialogo nao tratado: `onceDialog` foi configurado depois do clique;
- arquivo temporario mantido apos o teste: faltou limpeza em um bloco `finally`;
- teste instavel: foi utilizada uma espera fixa em vez de evento ou assertion web-first;
- execucao sempre visivel: o modo headless foi fixado no codigo em vez de ser obtido por `TestConfig` e `BrowserFactory`.

## 11. Rubrica e reflexao

- [ ] Os observadores sao registrados antes das acoes.
- [ ] O teste nao automatiza janelas nativas.
- [ ] O iframe e tratado dentro do proprio escopo.
- [ ] Os arquivos temporarios sao isolados e removidos.
- [ ] O popup e validado pela `Page` retornada pelo evento.
- [ ] O dialogo e tratado conscientemente.
- [ ] Nao existem esperas fixas.
- [ ] Cada teste recebe um novo `BrowserContext` e uma nova `Page`.
- [ ] Os timeouts definidos em `TestConfig` sao aplicados ao contexto e as assertions.

Perguntas finais:

1. Por que o popup compartilha cookies com a pagina principal?
2. Por que `setInputFiles` e mais estavel do que automatizar a janela do sistema?
3. Qual recurso e responsavel pelo ciclo de vida do download?
4. Por que o observador deve ser registrado antes do clique?
5. Quando `fill` e preferivel a digitacao caractere por caractere?

Consulte a solucao somente depois da autoavaliacao:

```powershell
.\course.ps1 solution 06
```

## Leituras oficiais

- [Downloads no Playwright Java](https://playwright.dev/java/docs/downloads)
- [Upload de arquivos](https://playwright.dev/java/docs/input#upload-files)
- [Frames](https://playwright.dev/java/docs/frames)
- [Paginas e popups](https://playwright.dev/java/docs/pages)
