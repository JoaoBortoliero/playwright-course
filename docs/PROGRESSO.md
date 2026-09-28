# Progresso e feedback

Este arquivo começa sem respostas. Use-o como diário técnico depois de cada exercício. Registre decisões e evidências, não apenas uma marca de conclusão.

| Aula | Situação | Evidência | Principal aprendizado |
|---:|---|---|---|
| 1 | Não iniciada | - | - |
| 2 | Não iniciada | - | - |
| 3 | Não iniciada | - | - |
| 4 | Não iniciada | - | - |
| 5 | Não iniciada | - | - |
| 6 | Não iniciada | - | - |
| 7 | Não iniciada | - | - |
| 8 | Não iniciada | - | - |
| 9 | Não iniciada | - | - |
| 10 | Não iniciada | - | - |
| 11 | Não iniciada | - | - |
| 12 | Não iniciada | - | - |

## Registro da Aula 1

1. Qual é a diferença entre `Browser` e `BrowserContext`?
2. Por que o teste fecha o contexto antes do browser?
3. O que o Playwright fez sem que você precisasse instalar um WebDriver?
4. Que parte do lifecycle você ainda não se sentiria seguro para explicar?

**Decisões, evidências e dúvidas:**

## Registro da Aula 2

1. Por que `Locator` não deve ser entendido como um `WebElement` armazenado?
2. Qual é a diferença entre auto-wait de uma ação e retry de uma assertion?
3. Por que `getByRole` costuma ser preferível a CSS quando ambos funcionam?
4. Em qual situação você escolheria conscientemente um test ID?
5. O que strictness protege e como você corrigiria um locator ambíguo?

**Decisões, evidências e dúvidas:**

## Registro da Aula 3

1. Quais objetos são compartilhados pela classe e quais nascem para cada teste?
2. Por que criar um contexto novo é melhor do que limpar cookies e storage?
3. O que `@TestInstance(PER_CLASS)` muda no lifecycle padrão do JUnit?
4. Por que os métodos `@BeforeAll` e `@AfterAll` podem não ser `static`?
5. Qual problema `setHeadless(false)` fixo causaria no Jenkins?
6. Se `@BeforeEach` criar o contexto, quem é responsável por fechá-lo e por quê?

**Decisões, evidências e dúvidas:**

## Registro da Aula 4

1. Por que `Locator` é uma consulta viva e `allTextContents()` representa uma fotografia?
2. Como validar uma coleção sem depender da ordem visual?
3. Por que preços devem ser convertidos para `BigDecimal`?
4. Quando a parametrização melhora ou prejudica a leitura?

**Decisões, evidências e dúvidas:**

## Registro da Aula 5

1. Quais transições da jornada de checkout exigem assertions intermediárias?
2. Por que produtos devem ser localizados pelo domínio, e não pela posição?
3. Como o teste comprova que subtotal e imposto formam o total?
4. Por que cada cenário negativo deve preparar o próprio estado?

**Decisões, evidências e dúvidas:**

## Registro da Aula 6

1. Por que o observador do evento deve ser registrado antes da ação?
2. Por que `setInputFiles` é preferível à automação da janela nativa?
3. Como o escopo de um `FrameLocator` difere do escopo da `Page`?
4. Como arquivos temporários são removidos mesmo quando o teste falha?

**Decisões, evidências e dúvidas:**

## Registro da Aula 7

1. Qual responsabilidade pertence ao teste e qual pertence ao Page Object?
2. Quando um componente deve ser separado de uma página?
3. Por que objetos de dados não devem depender de Playwright ou JUnit?
4. Quem é responsável pelo lifecycle ao usar `@PlaywrightTest`?

**Decisões, evidências e dúvidas:**

## Registro da Aula 8

1. Qual é a ordem correta de fechamento de `APIResponse`, `APIRequestContext` e `Playwright`?
2. Por que reutilizar `storageState` não significa reutilizar o mesmo contexto?
3. Que informações sensíveis podem existir no arquivo de estado?
4. Quando preparar dados por API é adequado?

**Decisões, evidências e dúvidas:**

## Registro da Aula 9

1. Por que `waitForResponse` deve filtrar URL e método?
2. O que um mock de rede não consegue provar?
3. Qual é a diferença entre `abort()` e uma resposta HTTP 503?
4. Como sincronizar a captura de um page error sem espera fixa?

**Decisões, evidências e dúvidas:**

## Registro da Aula 10

1. Por que cada thread deve criar e utilizar seus próprios objetos Playwright?
2. Que risco existe em compartilhar `Page` ou Page Object por campo estático?
3. O que a emulação mobile no Edge consegue validar e o que exige dispositivo real?
4. Como verificar foco por teclado sem afirmar que toda a acessibilidade foi testada?

**Decisões, evidências e dúvidas:**

## Registro da Aula 11

1. Quando uma screenshot é insuficiente para diagnosticar uma falha?
2. Qual é a diferença entre as políticas `off`, `on-failure` e `always`?
3. Por que o trace deve ser encerrado antes do fechamento do contexto?
4. Que informações nunca devem ser incluídas em anexos Allure?

**Decisões, evidências e dúvidas:**

## Registro da Aula 12

1. Por que o container precisa instalar o Microsoft Edge Stable?
2. Por que a pipeline executa os testes pelo `course.ps1`?
3. O que deve ser publicado no bloco `post { always { ... } }`?
4. Por que o job deve ser validado manualmente antes de ativar o timer diário?

**Decisões, evidências e dúvidas:**

## Modelo de registro

Ao concluir cada aula, registre:

- cenário implementado;
- comando executado pelo `course.ps1`;
- principal decisão e justificativa;
- uma falha diagnosticada;
- evidência gerada;
- o que o validador encontrou;
- resultado da rubrica;
- o que faria diferente em uma suíte de produção.
