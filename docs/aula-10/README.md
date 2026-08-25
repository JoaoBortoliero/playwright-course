# Aula 10 — Cross-browser, mobile, acessibilidade e paralelismo

Tempo sugerido: 5–6 horas. Pré-requisito: Aula 9 concluída.

## Objetivos

Projetar uma matriz de execução sustentável, emular dispositivos, fazer
verificações básicas de acessibilidade e executar em paralelo sem compartilhar
objetos Playwright entre threads.

## 1. Matriz baseada em risco

Rodar tudo em todos os browsers triplica custo e nem sempre triplica valor. A
estratégia do curso é:

- smoke pequena em Chromium, Firefox e WebKit;
- regressão completa em Chromium;
- cenários mobile relevantes com viewport/touch;
- expansão da matriz quando dados de produção justificarem.

Os motores do Playwright são Chromium, Firefox e WebKit; não são “Chrome,
Firefox e Safari instalados”. Canais de navegador podem ser configurados quando
essa compatibilidade específica fizer parte do risco.

O curso já transforma a propriedade em escolha de motor:

```powershell
.\course.ps1 exercise 10 -Browser chromium
.\course.ps1 exercise 10 -Browser firefox
.\course.ps1 exercise 10 -Browser webkit
```

O Java do teste não deve conter `playwright.chromium()` fixo. Essa decisão fica
em `CourseConfig` e `CourseBrowserFactory`.

### Tags e seleção

```java
@Test
@Tag("smoke")
@Tag("cross-browser")
void deveAbrirAplicacaoNosTresMotores(Page page) { }
```

Uma tag classifica; ela não executa o teste sozinha. Maven pode selecionar:

```powershell
.\mvn-local.ps1 test -Dgroups=smoke -Dbrowser=firefox
```

Smoke deve ser curta, representativa e independente. Não marque toda a
regressão como smoke apenas para obter mais números no pipeline.

Selenium normalmente distribui essa matriz com Grid e uma sessão WebDriver por
teste. Contextos Playwright são mais leves; em Java, porém, cada runtime deve
permanecer na thread que o criou, sem `Page` global.

## 2. Emulação não é aparelho real

Configure viewport, user agent, touch, locale, timezone e geolocalização no
`BrowserContext`. Emulação encontra problemas responsivos e de entrada, mas não
substitui hardware, WebView e sistema operacional reais.

```java
BrowserContext mobile = browser.newContext(
    new Browser.NewContextOptions()
        .setBaseURL(lab.baseUrl())
        .setViewportSize(390, 844)
        .setHasTouch(true)
        .setIsMobile(true));
try {
  Page mobilePage = mobile.newPage();
  mobilePage.navigate("/");
  assertEquals(390, mobilePage.viewportSize().width);
} finally {
  mobile.close();
}
```

As opções pertencem ao `BrowserContext`, porque duas sessões do mesmo browser
podem representar dispositivos diferentes. `isMobile` não é suportado pelo
Firefox; mantenha o exercício de emulação em Chromium e deixe a smoke web
portável para os três motores.

Locators por role incentivam HTML acessível. Isso não é auditoria completa.
Verifique nome acessível, foco por teclado e landmarks; ferramentas como axe
podem ser integradas num projeto real, mas não substituem testes com pessoas.

Uma verificação mínima por teclado:

```java
page.keyboard().press("Tab");
assertThat(page.getByRole(AriaRole.LINK,
    new Page.GetByRoleOptions().setName("Interações"))).isFocused();
```

Isso verifica uma interação concreta, não “a acessibilidade inteira”.

## 3. Paralelismo seguro

Playwright Java não é thread-safe. Uma instância e seus objetos devem ser usados
na thread que os criou. A extensão mantém runtime/browser por thread e cria
contexto por teste. Nunca use `static Page`, singleton de Page Object ou lista
mutável de dados compartilhada.

Ative paralelismo do JUnit com propriedades controladas e comece com poucas
threads. Paralelismo revela colisão de dados; não “conserte” com locks globais
que serializam a suíte inteira.

O arquivo `junit-platform.properties` já define modo concorrente e duas
threads, mas deixa o recurso desligado. Ative apenas na execução de laboratório:

```powershell
.\mvn-local.ps1 test `
  -Dtest=PortabilidadeEParalelismoExercicioTest `
  -Djunit.jupiter.execution.parallel.enabled=true
```

Com `@PlaywrightTest`, cada thread obtém seu próprio runtime/browser e cada
invocação recebe contexto/página novos. Não mova `Page`, `Locator` ou Page
Object para campos `static`.

Para observar sem compartilhar estado, registre localmente:

```java
long threadId = Thread.currentThread().getId();
String browser = config.browser();
System.out.printf("thread=%d browser=%s%n", threadId, browser);
```

## 4. Exercício

```powershell
.\course.ps1 demo 10 -Browser firefox
```

Implemente `PortabilidadeEParalelismoExercicioTest`:

1. marque uma smoke do laboratório como `smoke` e `cross-browser`;
2. execute-a nos três motores por propriedade;
3. crie contexto mobile com viewport 390×844 e touch;
4. valide navegação por teclado e nomes acessíveis;
5. crie dois testes independentes aptos a paralelo;
6. execute com `junit.jupiter.execution.parallel.enabled=true`.

Implemente primeiro a smoke desktop e execute nos três motores. Depois crie um
teste mobile separado apenas em Chromium. Por fim adicione um segundo teste
independente e ative duas threads. Mudar as três dimensões ao mesmo tempo torna
qualquer falha difícil de diagnosticar.

### Desafio independente

Registre id da thread, browser e teste sem usar estado mutável compartilhado.
Analise a execução e explique por que `ThreadLocal<Page>` usado pelo teste seria
uma abstração perigosa mesmo que pareça funcionar.

## Validação e rubrica

```powershell
.\course.ps1 validate 10
```

- [ ] Smoke é pequena e representativa.
- [ ] Emulação está no contexto, não em CSS/JavaScript da página.
- [ ] Nenhum objeto Playwright é compartilhado entre threads.
- [ ] Dados de teste são únicos ou imutáveis.
- [ ] Acessibilidade não é reduzida a um único scanner.

Perguntas: qual falha só um dispositivo real encontra? Que cenários merecem os
três motores? Como distinguir flakiness de colisão de dados?

Erros comuns: mensagem sobre `isMobile` no Firefox indica matriz inadequada;
falhas que desaparecem sem paralelismo sugerem estado compartilhado ou dados
colidindo; browser fixo no código impede que `-Browser` tenha efeito.

Leituras: [browsers](https://playwright.dev/java/docs/browsers),
[emulation](https://playwright.dev/java/docs/emulation),
[multithreading](https://playwright.dev/java/docs/multithreading) e
[accessibility testing](https://playwright.dev/java/docs/accessibility-testing).

Solução após a autoavaliação: `.\course.ps1 solution 10`.
