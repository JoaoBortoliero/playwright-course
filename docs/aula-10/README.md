# Aula 10 - Mobile, acessibilidade e paralelismo

Tempo sugerido: 5 a 6 horas. Pré-requisito: Aula 9 concluída.

## Objetivos

Emular dispositivos, realizar verificações básicas de acessibilidade e executar testes em paralelo sem compartilhar objetos Playwright entre threads.

## 1. Estratégia de execução

O Microsoft Edge instalado na máquina é o único navegador utilizado pelo curso.

Por padrão, os testes executam com o navegador visível:

```powershell
.\course.ps1 demo 10
```

Para executar sem exibir o navegador:

```powershell
.\course.ps1 demo 10 -Headless
```

A criação do Microsoft Edge permanece centralizada em `CourseBrowserFactory`. Os testes não devem iniciar ou selecionar outros navegadores.

### Tags

Uma tag permite classificar e selecionar grupos de testes.

Para executar somente a smoke:

```powershell
.\course.ps1 exercise 10 -Groups smoke
```

A smoke deve ser curta, representativa e independente. Não marque toda a regressão como smoke.

## 2. Emulação e acessibilidade

As opções de emulação pertencem ao `BrowserContext`:

```java
BrowserContext mobile = browser.newContext(
        new Browser.NewContextOptions()
                .setBaseURL(lab.baseUrl())
                .setViewportSize(390, 844)
                .setHasTouch(true)
                .setIsMobile(true));
```

O cenário mobile utiliza o Microsoft Edge instalado. A emulação permite validar viewport, touch e comportamento responsivo, mas não substitui um dispositivo real.

Locators por role ajudam a criar testes alinhados à acessibilidade, mas não representam uma auditoria completa. Nesta aula, valide nome acessível e foco por teclado.

## 3. Paralelismo seguro

Playwright Java não é thread-safe. Cada teste deve criar, utilizar e fechar `Playwright`, `Browser`, `BrowserContext` e `Page` na mesma thread.

Não utilize objetos Playwright estáticos, singleton de Page Object ou coleções mutáveis compartilhadas entre testes.

Para ativar o paralelismo:

```powershell
.\course.ps1 exercise 10 -Parallel
```

Cada teste deve registrar localmente o identificador da thread, sem compartilhar estado.

## 4. Exercício

Implemente `PortabilidadeEParalelismoExercicioTest`:

1. mantenha uma smoke com a tag `smoke`;
2. use `CourseConfig` e `CourseBrowserFactory`;
3. utilize somente o Microsoft Edge instalado;
4. crie um contexto mobile com viewport 390 x 844 e touch;
5. valide foco por teclado e nome acessível;
6. implemente um segundo teste desktop independente;
7. execute os testes com `-Parallel`.

Implemente primeiro a smoke desktop. Depois crie o cenário mobile e, por último, o segundo cenário desktop independente.

## Validação

```powershell
.\course.ps1 validate 10
```

- [ ] A smoke é pequena e representativa.
- [ ] Somente o Microsoft Edge instalado é utilizado.
- [ ] A emulação está configurada no `BrowserContext`.
- [ ] Nenhum objeto Playwright é compartilhado entre threads.
- [ ] Cada teste fecha os recursos que criou.
- [ ] Os dados de teste são únicos ou imutáveis.
- [ ] Acessibilidade não é reduzida a um único scanner.

## Leituras

- [Emulation](https://playwright.dev/java/docs/emulation)
- [Multithreading](https://playwright.dev/java/docs/multithreading)
- [Accessibility testing](https://playwright.dev/java/docs/accessibility-testing)

Solução após a autoavaliação:

```powershell
.\course.ps1 solution 10
```
