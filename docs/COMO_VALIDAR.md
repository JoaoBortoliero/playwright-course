# Como funciona a correção

## Quatro fontes de evidência

1. **Execução:** o exercício compila, executa e observa o comportamento real.
2. **Regras de fonte:** JavaParser encontra esperas fixas, locators frágeis e violações objetivas.
3. **Arquitetura:** a partir da Aula 7, ArchUnit verifica dependências e ciclos.
4. **Rubrica e solução:** avaliam intenção, legibilidade e modelagem.

Um teste vazio pode ficar verde. Por isso, passar não significa estar bem testado.

## Validar uma aula

```powershell
.\course.ps1 validate 04
```

Para validar sem exibir o Edge:

```powershell
.\course.ps1 validate 04 -Headless
```

O comando executa o exercício, o validador de fonte e, a partir da Aula 7, o validador de arquitetura.

Os validadores aceitam soluções equivalentes e não comparam o código com o gabarito. Comentários com o nome de uma API não contam como implementação.

## Quando consultar a solução

Consulte `reference-solutions` somente depois de:

1. executar uma tentativa;
2. registrar a hipótese da falha;
3. consultar as dicas;
4. executar o validador.

```powershell
.\course.ps1 solution 04
```

Compare responsabilidades, locators, assertions e isolamento. Uma solução diferente não precisa ser reescrita quando possui qualidade equivalente.

## Evidências de validação

Registre em `docs/PROGRESSO.md`:

- comando executado;
- resultado do exercício;
- mensagens dos validadores;
- screenshot ou trace quando relevante;
- decisões da rubrica.

## Limites da automação

Nomes expressivos, cobertura relevante, segurança de dados e simplicidade exigem julgamento humano. A rubrica manual deve acompanhar qualquer pedido de revisão.
