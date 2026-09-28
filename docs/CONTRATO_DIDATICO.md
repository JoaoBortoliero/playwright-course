# Contrato didático do curso

Este curso parte de conhecimento de Java e Selenium, mas não pressupõe conhecimento prévio de Playwright. A progressão vai do primeiro teste a uma suíte com arquitetura, paralelismo, evidências e Jenkins.

## Guardrails do projeto

- somente Microsoft Edge instalado;
- execução visível por padrão;
- execução não visível apenas com `-Headless`;
- comandos orientados pelo `course.ps1`;
- `mvn-local.ps1` usado apenas internamente;
- exercícios iniciais não entregam a solução;
- nenhuma espera fixa;
- um novo `BrowserContext` para cada teste;
- nenhum objeto Playwright compartilhado entre threads.

## Regra de cada aula

Todo módulo segue esta ordem:

1. **modelo mental:** problema e diferença relevante para Selenium;
2. **API nova:** assinatura, objetos envolvidos e exemplo mínimo;
3. **demonstração executável:** aplicação local ou SauceDemo;
4. **exercício guiado:** partes pequenas com critérios intermediários;
5. **desafio independente:** somente conhecimentos ensinados;
6. **correção:** execução, validador, rubrica e solução separada.

Um exercício pode exigir conhecimentos anteriores, mas deve informá-los. Não pode exigir silenciosamente uma API futura.

## Como estudar uma API nova

Para cada chamada, responda:

```text
Quem oferece o método?  Page, Locator, BrowserContext, APIRequestContext...
O que ele recebe?       papel, texto, caminho, callback, opções...
O que ele devolve?      Locator, Response, Download, Page, void...
Quando sincroniza?      ação, evento, assertion com retry ou captura imediata?
```

Exemplo:

```java
Download download = page.waitForDownload(
    () -> page.getByText("Baixar").click());
```

- dono: `Page`;
- entrada: callback que dispara o download;
- retorno: `Download`;
- sincronização: observador registrado antes do clique.

## Responsabilidades

O projeto fornece configuração, laboratório local, demonstrações, arquivos iniciais, validadores e soluções separadas. O aluno implementa os testes e as abstrações indicadas.

As soluções devem ser consultadas depois de uma tentativa registrada.

## Como pedir revisão

Envie:

- arquivo alterado;
- comando executado pelo `course.ps1`;
- falha completa;
- evidência relevante;
- respostas da rubrica.

A revisão deve avaliar comportamento e decisões, não apenas confirmar que o teste ficou verde.
