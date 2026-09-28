# Aula 12 - Docker, Jenkins e projeto final

Tempo sugerido: 6 a 10 horas. Pré-requisito: Aulas 1 a 11 concluídas.

## Objetivos

Executar a suíte de forma reprodutível em container, montar uma pipeline Jenkins, publicar resultados e entregar o projeto final do SauceDemo.

## 1. Ambiente reprodutível com Microsoft Edge

O curso utiliza exclusivamente o Microsoft Edge. Localmente, o projeto usa o Edge instalado na máquina. No container, o `Dockerfile` instala o Microsoft Edge Stable e o PowerShell para executar o mesmo `course.ps1`.

A imagem base deve usar a mesma versão da biblioteca Playwright declarada no `pom.xml`.

### Construção

```powershell
docker build -t playwright-course-edge:1.0 .
```

### Execução no container

```powershell
docker run --rm --init --ipc=host `
  -v "${PWD}:/work" -w /work `
  playwright-course-edge:1.0 `
  pwsh -File ./course.ps1 demo 12 -Headless
```

O download automático dos navegadores Playwright permanece desabilitado. O Edge instalado na imagem é iniciado pelo canal `msedge` configurado em `CourseBrowserFactory`.

## 2. Pipeline Jenkins

A pipeline utiliza somente Edge:

```text
Build da imagem -> Setup -> Validação -> Smoke Edge -> Regressão Edge -> Publicação
```

Os testes são executados pelo `course.ps1`:

```powershell
.\course.ps1 exercise 12 -Groups smoke -Headless
.\course.ps1 exercise 12 -Groups regression -Headless -Parallel
```

O bloco `post { always { ... } }` publica XML JUnit, resultados Allure e evidências existentes em `artifacts`, mesmo quando a execução falha.

A agenda diária utiliza:

```groovy
triggers {
    cron('H 2 * * *')
}
```

Antes de ativar o timer, execute o job manualmente, valide as publicações, confirme o fuso do controller e a disponibilidade do agente Docker. `disableConcurrentBuilds()` impede sobreposição.

## 3. Projeto final

Construa a suíte em `br.com.curso.playwright.capstone` com:

- login válido, inválido, bloqueado e campos obrigatórios;
- catálogo, detalhes, ordenação e consistência dos produtos;
- adicionar e remover produtos e validar o badge;
- checkout obrigatório, subtotal, imposto, total e conclusão;
- logout e reset de estado;
- isolamento e paralelismo;
- tags `smoke`, `regression` e `negative`;
- evidências em falha.

Use `@PlaywrightTest`. Não copie o lifecycle manual. Page Objects não devem conter assertions.

## 4. Exercício

```powershell
.\course.ps1 exercise 12
.\course.ps1 validate 12
.\course.ps1 exercise 12 -Headless -Parallel
```

Implemente em incrementos verdes:

1. login e catálogo;
2. header e carrinho;
3. checkout e cálculo monetário;
4. negativos parametrizados;
5. logout e reset;
6. tags e isolamento;
7. paralelismo;
8. evidências;
9. container;
10. job manual e timer diário.

## Rubrica final

- estratégia e cobertura: 20;
- locators e assertions: 15;
- isolamento e paralelismo: 15;
- arquitetura e modelagem: 15;
- dados, configuração e segredos: 10;
- diagnóstico e evidências: 10;
- mobile e acessibilidade: 5;
- Jenkins e reprodutibilidade: 10.

Critérios de aceite:

- versões Maven e imagem compatíveis;
- somente Microsoft Edge;
- smoke e regressão separadas por tags;
- XML, Allure e evidências preservados em falhas;
- concorrência de builds desabilitada;
- execução manual validada antes do timer;
- horário, fuso e agente documentados.

## Leituras

- [CI](https://playwright.dev/java/docs/ci)
- [Docker](https://playwright.dev/java/docs/docker)
- [Pipeline e triggers](https://www.jenkins.io/doc/book/pipeline/syntax/#triggers)
- [Jenkins JUnit](https://www.jenkins.io/doc/pipeline/steps/junit/)
- [Boas práticas](https://playwright.dev/java/docs/best-practices)

```powershell
.\course.ps1 solution 12
```
