# Aula 12 — Docker, Jenkins e projeto final

Tempo sugerido: 6–10 horas. Pré-requisito: Aulas 1 a 11 concluídas.

## Objetivos

Executar de forma reprodutível em Docker, montar um pipeline Jenkins, publicar
resultados e entregar uma suíte profissional do SauceDemo.

## 1. Ambiente reprodutível

A imagem `mcr.microsoft.com/playwright/java:v1.62.0-noble` contém browsers e
dependências de sistema. A versão da imagem deve ser exatamente a da biblioteca
Maven. Fixar tag evita que atualização silenciosa quebre a compatibilidade.

O container executa código confiável do curso. Para sites não confiáveis, leia
as recomendações de usuário não-root e seccomp da documentação oficial.

Local e CI diferem em display, filesystem, CPU e rede. Headless é padrão; não
aumente timeout global como primeira resposta a uma máquina lenta. Descubra a
condição que não foi observada.

Selenium Grid continua adequado a browsers e ambientes remotos. Aqui a imagem
Playwright fornece os três motores no agente. A disciplina de CI é a mesma:
versões fixas, isolamento, XML JUnit e artefatos sempre publicados.

### Executando a mesma imagem manualmente

```bash
docker run --rm --init --ipc=host \
  -v "$PWD:/work" -w /work \
  mcr.microsoft.com/playwright/java:v1.62.0-noble \
  mvn -B -ntp test
```

| Parte | Motivo |
|---|---|
| `--rm` | remove o container após a execução |
| `--init` | encerra processos filhos dos browsers corretamente |
| `--ipc=host` | reduz problemas de memória compartilhada do Chromium |
| `-v ...:/work` | monta o repositório dentro do container |
| `-w /work` | define a pasta que contém `pom.xml` |
| tag `v1.62.0-noble` | alinha imagem e biblioteca Playwright |

No Windows, Docker Desktop precisa usar containers Linux. No Jenkins, o agente
precisa ter permissão para usar Docker; instalá-lo apenas no controller não
torna todos os agentes compatíveis.

## 2. Pipeline como produto

O `Jenkinsfile` possui estágios independentes:

```text
compile -> validators -> smoke x 3 -> regression Chromium -> publish
```

Falhas de teste publicam XML e artefatos no `post { always { ... } }`. Allure é
relatório rico; `junit` define o status e histórico nativo. Traces e screenshots
são arquivados principalmente em falhas.

### Anatomia do Jenkinsfile

```groovy
pipeline {
  agent { docker { /* imagem Playwright */ } }
  options { /* retenção, timestamps, concorrência */ }
  triggers { /* agenda */ }
  environment { /* configuração não secreta */ }
  stages { /* compile, validação e testes */ }
  post { always { /* publicar mesmo quando falhar */ } }
}
```

Smoke usa tags, por exemplo:

```bash
mvn -B -ntp -Dgroups=smoke -Dbrowser=firefox test
```

A regressão completa usa Chromium para controlar custo. `post { always { ... }
}` publica resultados mesmo se um estágio falhar; publicar não transforma um
teste vermelho em verde.

Segredos entram por Jenkins Credentials e são expostos no menor escopo. Nunca
use `echo`, nome de arquivo ou anexo que revele seu valor.

### Execução automática diária

Uma pipeline profissional não depende de alguém lembrar de clicar em **Build
Now**. O bloco abaixo agenda a suíte diariamente durante a madrugada:

```groovy
triggers {
  cron('H 2 * * *')
}
```

A expressão possui minuto, hora, dia do mês, mês e dia da semana. `H` é um hash
estável baseado no nome do job: neste exemplo o build começa uma vez por dia,
em algum minuto entre 02:00 e 02:59. Isso distribui a carga melhor do que iniciar
todos os projetos exatamente às 02:00. A agenda usa o fuso do controller
Jenkins, que deve ser confirmado antes da ativação.

O timer apenas solicita a execução. Controller e agente precisam estar
disponíveis, e o agente deve oferecer Docker. `disableConcurrentBuilds()` evita
que duas execuções desta pipeline disputem o mesmo ambiente quando uma noite
demorar mais do que o esperado.

Leia [o guia operacional do Jenkins](../JENKINS.md) antes do exercício final.

### Primeiro job, sem lacunas

1. publique o repositório Git;
2. crie **New Item → Pipeline**;
3. selecione **Pipeline script from SCM**;
4. configure Git, credencial, branch e Script Path `Jenkinsfile`;
5. execute manualmente antes de confiar no timer;
6. confirme a causa e o horário calculado pelo Jenkins;
7. mantenha controller e agente disponíveis durante a madrugada.

Webhook e cron resolvem problemas diferentes: webhook reage a mudanças; cron
executa mesmo sem commit novo e detecta mudanças de ambiente ou aplicação.

## 3. Projeto final

Construa a suíte em `br.com.curso.playwright.capstone` usando os componentes da
Aula 7. Cobertura obrigatória:

- login válido, inválido, bloqueado e campos obrigatórios;
- catálogo, detalhes, ordenação e consistência dos produtos;
- adicionar/remover carrinho e badge;
- checkout obrigatório, subtotal, imposto, total e conclusão;
- logout e reset de estado;
- contexto isolado e execução paralela;
- tags smoke/regression/negative;
- evidências em falha.

Critérios não funcionais: locators resilientes, zero espera fixa, dados
centralizados, nenhuma dependência de ordem, Page Objects sem assertions,
diagnóstico legível e execução individual.

### Planejamento antes do código

| Área | Smoke | Regressão | Negative |
|---|---|---|---|
| login padrão | sim | sim | não |
| usuário bloqueado | não | sim | sim |
| catálogo | sim | sim | não |
| compra completa | sim | sim | não |
| campos obrigatórios | não | sim | sim |
| logout/reset | não | sim | não |

`negative` descreve intenção; `smoke` e `regression` descrevem conjuntos de
execução. Uma mesma classe pode conter tags diferentes por método.

Estrutura sugerida:

```text
capstone/
  tests/          comportamento e assertions
  pages/          telas e ações de domínio
  components/     header/menu compartilhado
  data/           Customer, Product e casos
support/
  config/         já fornecido
  junit/          extensão e evidências já fornecidas
```

Não copie o lifecycle manual no projeto final. Use `@PlaywrightTest` e injete
`Page`/`CourseConfig`.

## 4. Exercício

Implemente `ProjetoFinalExercicioTest` e os Page Objects necessários. Execute:

```powershell
.\course.ps1 exercise 12
.\course.ps1 validate 12
.\course.ps1 exercise 12 -Browser firefox
```

Implemente em incrementos verdes:

1. smoke de login e catálogo;
2. componente de header e carrinho;
3. checkout e cálculo monetário;
4. negativos parametrizados;
5. logout e reset;
6. tags e execução isolada;
7. paralelismo com duas threads;
8. evidências em uma falha controlada;
9. Docker;
10. job manual e somente então timer diário.

Não depure Page Objects, três browsers, Docker e Jenkins ao mesmo tempo. Cada
incremento deve ter um critério objetivo de conclusão.

Depois execute no mesmo container do Jenkins:

```bash
docker run --rm --init --ipc=host -v "$PWD:/work" -w /work \
  mcr.microsoft.com/playwright/java:v1.62.0-noble mvn test
```

Crie então um job **Pipeline script from SCM**, execute-o manualmente e valide
as publicações. Em uma branch de laboratório, troque temporariamente a agenda
por `H/5 * * * *`, confirme um disparo iniciado pelo timer e restaure
`H 2 * * *` antes de integrar a mudança. Documente o fuso do controller e onde
o agente ficará disponível durante a madrugada.

### Desafio independente

Introduza intencionalmente um locator frágil, uma colisão de dados e uma falha
de assertion. Use Jenkins/trace para diferenciar as três causas e documente o
tempo de diagnóstico. Reverta todas antes da entrega.

## Rubrica final (100 pontos)

- estratégia e cobertura: 15;
- locators/assertions: 15;
- isolamento e paralelismo: 15;
- arquitetura/modelagem: 15;
- dados/configuração/segredos: 10;
- diagnóstico e evidências: 10;
- cross-browser/mobile: 10;
- Jenkins e reprodutibilidade: 10.

Nos 10 pontos de Jenkins estão incluídos: job vindo do SCM, primeira execução
manual, timer diário validado, prevenção de concorrência e publicação de
resultados/artefatos em falhas.

Critérios de aceite da pipeline:

- biblioteca Maven e imagem Docker usam a mesma versão;
- smoke executa nos três motores;
- regressão executa em Chromium;
- falha mantém XML, Allure e evidências;
- `disableConcurrentBuilds()` impede sobreposição;
- timer temporário foi observado e restaurado para `H 2 * * *`;
- horário/fuso e disponibilidade do agente estão documentados.

Para aprovação: mínimo 75, nenhum segredo versionado, nenhuma espera fixa,
nenhuma dependência de ordem e smoke verde nos três motores.

Perguntas finais: qual risco sua suíte não cobre? Que teste removeria primeiro
se a execução dobrasse de duração? Qual sinal acompanharia para medir
flakiness? Como você defenderia a matriz ao time?

Erros comuns: `docker: command not found` é infraestrutura do agente; browser
ausente sugere imagem/versão errada; pipeline sem testes pode indicar filtro de
tags vazio; timer ausente exige verificar se o job carregou o Jenkinsfile, o
fuso e a disponibilidade do controller.

Leituras: [CI](https://playwright.dev/java/docs/ci),
[Docker](https://playwright.dev/java/docs/docker),
[sintaxe de Pipeline e triggers](https://www.jenkins.io/doc/book/pipeline/syntax/#triggers),
[Jenkins JUnit](https://www.jenkins.io/doc/pipeline/steps/junit/) e
[boas práticas](https://playwright.dev/java/docs/best-practices).

Solução após a autoavaliação: `.\course.ps1 solution 12`.
