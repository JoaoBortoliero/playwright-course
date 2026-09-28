# Aula 11 - Depuração, evidências e Allure

Tempo sugerido: 4 a 5 horas. Pré-requisito: Aula 10 concluída.

## Objetivos

Escolher a evidência adequada, usar Inspector e Trace Viewer, capturar screenshot, vídeo e trace, e publicar resultados legíveis no Allure.

## 1. Diagnóstico antes da repetição

Uma falha útil deve permitir identificar o passo, o estado da página, a URL e os eventos relevantes. Reexecutar até o teste ficar verde pode eliminar a evidência de uma falha intermitente.

Use:

- execução visível para observação inicial;
- `PWDEBUG=1` e Inspector para explorar locators;
- screenshot para o estado visual final;
- trace para ações, DOM, rede, console e código-fonte;
- vídeo para observar a sequência geral.

### Inspector no Windows

```powershell
$env:PWDEBUG = "1"
.\course.ps1 exercise 11
Remove-Item Env:PWDEBUG
```

O Edge já executa visível por padrão. Remova a variável ao terminar para evitar que uma execução automatizada aguarde interação manual.

## 2. Política de evidência

Evidências ficam em `artifacts` e não devem entrar no Git. Evite anexar senhas, tokens, cookies ou dados pessoais.

| Valor | Comportamento |
|---|---|
| `off` | não mantém a evidência |
| `on-failure` | mantém somente quando o teste falha |
| `always` | mantém em sucesso e falha |

O padrão do curso é:

- trace: `on-failure`;
- screenshot: `on-failure`;
- vídeo: `off`.

Para manter trace e screenshot em uma execução bem-sucedida:

```powershell
.\course.ps1 exercise 11 -Trace always -Screenshot always
```

Para ativar também o vídeo:

```powershell
.\course.ps1 exercise 11 -Trace always -Screenshot always -Video always
```

O vídeo é configurado na criação do `BrowserContext` e finalizado quando o contexto fecha. A política define se o arquivo gravado será mantido ou removido.

Para abrir um trace produzido pela execução:

```powershell
.\course.ps1 trace -TracePath artifacts\seu-trace.zip
```

O trace deve ser encerrado antes do fechamento do contexto.

## 3. Allure

O adapter Allure registra resultados em `target/allure-results`. Steps devem representar ações relevantes de negócio, não cada chamada técnica do Playwright.

Exemplo:

```java
Allure.step("Finalizar pedido", () ->
        page.getByText("Finalizar").click());
```

Os anexos devem ser úteis e não podem conter segredos. O XML JUnit continua sendo a fonte do status da execução no pipeline.

## 4. Exercício

Execute a demonstração:

```powershell
.\course.ps1 demo 11 -Trace always -Screenshot always
```

Implemente `EvidenciasExercicioTest`:

1. crie steps Allure para preparar, agir e verificar;
2. anexe texto diagnóstico sem informações sensíveis;
3. capture uma screenshot explicativa;
4. localize os arquivos produzidos em `artifacts`;
5. abra o trace e identifique ação, locator, DOM e request;
6. provoque uma falha controlada, analise as evidências e restaure o teste.

Depois, execute com a política padrão e confirme que uma execução bem-sucedida não acumula evidências configuradas como `on-failure`.

## Validação

```powershell
.\course.ps1 validate 11
```

- [ ] Evidências aparecem quando a política determina.
- [ ] A captura não esconde o erro original.
- [ ] O teardown ocorre mesmo quando a captura falha.
- [ ] Os anexos não contêm segredos.
- [ ] Os steps representam ações relevantes.
- [ ] A causa da falha pode ser explicada pelo trace.

## Leituras

- [Debug](https://playwright.dev/java/docs/debug)
- [Trace Viewer](https://playwright.dev/java/docs/trace-viewer)
- [Videos](https://playwright.dev/java/docs/videos)
- [Allure JUnit 5](https://allurereport.org/docs/junit5/)

Solução após a autoavaliação:

```powershell
.\course.ps1 solution 11
```
