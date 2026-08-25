# Progresso e feedback

Este arquivo começa sem respostas. Use-o como diário técnico depois de cada
exercício; registre decisões e evidências, não apenas uma marca de conclusão.

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

## Registros das Aulas 4–12

Crie uma subseção ao concluir cada aula e registre:

- cenário implementado;
- principal decisão e justificativa;
- uma falha diagnosticada;
- o que o validador encontrou;
- resultado da rubrica;
- o que faria diferente em uma suíte de produção.
