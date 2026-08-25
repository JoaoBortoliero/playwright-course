# Como funciona a correção

## Quatro fontes de evidência

1. **Execução:** o exercício compila, executa e observa o comportamento real.
2. **Regras de fonte:** JavaParser encontra esperas fixas, locators frágeis e
   violações objetivas adequadas à aula.
3. **Arquitetura:** a partir da Aula 7, ArchUnit verifica dependências e ciclos.
4. **Rubrica e gabarito:** avaliam intenção, legibilidade e modelagem.

Um teste vazio pode ficar verde; por isso “passou” nunca é sinônimo de “está
bem testado”. Leia todas as mensagens e responda à rubrica.

```powershell
.\course.ps1 validate 04
```

Os validadores aceitam soluções equivalentes. Eles não comparam seu arquivo
com o gabarito. Cada regra informa o motivo e o conceito que precisa ser revisto.

Comentários com o nome de uma API não contam como implementação. Os validadores
inspecionam anotações, tipos e chamadas reais sempre que possível. Ainda assim,
eles não conseguem avaliar sozinhos se o oráculo é relevante; por isso a
rubrica manual é obrigatória.

## Quando consultar a solução

Consulte `reference-solutions` somente depois de executar uma tentativa,
registrar a hipótese de falha, usar as dicas e rodar o validador.

```powershell
.\course.ps1 solution 04
```

Compare responsabilidades, locators, assertions e isolamento. Se o seu código
for diferente, mas tiver a mesma qualidade, não há obrigação de reescrevê-lo.

## Limites da automação

Nomes expressivos, cobertura relevante e simplicidade exigem julgamento. A
rubrica de cada aula é a parte manual da avaliação e deve acompanhar qualquer
pedido de revisão ao professor.
