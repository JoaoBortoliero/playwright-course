# Contrato didático do curso

Este curso parte de conhecimento de Java e Selenium, mas não pressupõe
conhecimento prévio de Playwright. A progressão vai do primeiro browser a uma
suíte com arquitetura, paralelismo, evidências e Jenkins.

## Regra de cada aula

Todo módulo segue esta ordem:

1. **modelo mental:** o problema e a diferença relevante para Selenium;
2. **API nova:** assinatura, objetos envolvidos e exemplo mínimo;
3. **demonstração executável:** aplicação local ou SauceDemo com comentários;
4. **exercício guiado:** partes pequenas, cada uma com critério intermediário;
5. **desafio independente:** somente conhecimentos ensinados; pesquisa externa
   é indicada explicitamente quando fizer parte do objetivo;
6. **correção:** execução, validador, rubrica e solução separada.

Um exercício pode exigir conhecimentos de aulas anteriores, mas deve informar
quais são. Não pode exigir silenciosamente uma API futura.

## Como estudar uma API nova

Para cada chamada, responda quatro perguntas:

```text
Quem oferece o método?     Page, Locator, BrowserContext, APIRequestContext...
O que ele recebe?          papel, texto, caminho, callback, opções...
O que ele devolve?         Locator, Response, Download, Page, void...
Quando ele sincroniza?     ação, evento, assertion com retry ou captura imediata?
```

Exemplo:

```java
Download download = page.waitForDownload(
    () -> page.getByText("Baixar").click());
```

- dono do método: `Page`;
- entrada: uma ação que dispara o download;
- retorno: `Download`;
- sincronização: o observador é registrado antes do clique e termina quando o
  evento ocorre.

## O que é fornecido e o que é responsabilidade do aluno

O projeto fornece configuração, laboratório local, demonstrações, arquivos
iniciais, validadores e gabaritos separados. O aluno implementa os testes e,
quando a aula for de arquitetura, as abstrações indicadas.

As soluções completas devem ser consultadas depois de uma tentativa. Exemplos
teóricos podem mostrar integralmente uma técnica isolada; isso é ensino, não o
gabarito da atividade inteira.

## Como pedir revisão

Envie o arquivo alterado, o comando executado, a falha completa e suas respostas
da rubrica. Uma revisão deve avaliar comportamento e decisões, não apenas dizer
que o teste ficou verde.
