# Jenkins, Docker e execução diária

## Pré-requisitos

O controller Jenkins precisa estar em execução e possuir um agente Linux com
Docker disponível. Instale os plugins Pipeline, Git e Allure Jenkins Plugin. A
imagem oficial já contém browsers e bibliotecas do sistema; o Maven baixa
somente as dependências Java.

Se o Jenkins estiver no computador pessoal e ele estiver desligado durante a
madrugada, a execução não ocorrerá. Para uma agenda confiável, mantenha o
controller e pelo menos um agente compatível disponíveis no horário programado.

## Criar o job a partir do repositório

1. No Jenkins, selecione **New Item** e crie um job do tipo **Pipeline**.
2. Em **Pipeline**, escolha **Pipeline script from SCM**.
3. Selecione Git, informe a URL do repositório e a credencial quando necessária.
4. Informe a branch que contém o curso e mantenha `Jenkinsfile` como Script Path.
5. Salve e execute **Build Now** antes de confiar no agendamento.
6. Confirme os estágios, os resultados JUnit, o relatório Allure e os artefatos.

Pipeline como código mantém estágios, versões e agenda revisáveis junto com os
testes. Um webhook pode iniciar builds em mudanças do GitHub; o timer diário é
independente do webhook.

## Como funciona a agenda

O `Jenkinsfile` contém:

```groovy
triggers {
  cron('H 2 * * *')
}
```

Os cinco campos representam minuto, hora, dia do mês, mês e dia da semana.
`H 2 * * *` executa uma vez por dia em um minuto estável escolhido pelo Jenkins
entre 02:00 e 02:59. O `H` distribui a carga entre os jobs e não muda a cada
dia. Para exigir exatamente 02:00 seria possível usar `0 2 * * *`, mas isso
pode concentrar muitas pipelines no mesmo instante.

O horário segue o fuso configurado no controller Jenkins. Valide o fuso antes
de publicar a agenda, especialmente quando controller, agente e equipe estão em
regiões diferentes. O bloco `disableConcurrentBuilds()` evita duas execuções da
mesma pipeline simultaneamente: se a execução anterior ainda estiver ativa, a
próxima aguardará.

Referência: [triggers e sintaxe cron do Jenkins](https://www.jenkins.io/doc/book/pipeline/syntax/#triggers).

## Exercício seguro do agendamento

Depois que uma execução manual estiver verde:

1. em uma branch de laboratório, altere temporariamente para
   `cron('H/5 * * * *')`;
2. publique a branch e deixe o Jenkins carregar o novo `Jenkinsfile`;
3. aguarde uma execução iniciada por timer e confira a causa do build;
4. valide relatórios e artefatos mesmo se um teste falhar;
5. restaure `cron('H 2 * * *')` e integre somente a agenda diária.

Não deixe a expressão de cinco minutos na branch principal. O teste temporário
serve para comprovar o mecanismo sem esperar até a madrugada.

## Segredos

Para segredos reais, crie uma credencial no Jenkins e limite-a ao estágio:

```groovy
withCredentials([usernamePassword(credentialsId: 'test-user',
  usernameVariable: 'TEST_USER', passwordVariable: 'TEST_PASSWORD')]) {
  sh 'mvn -B test'
}
```

Não imprima variáveis, cookies ou storage state. Mantenha a imagem Docker e
`playwright.version` em 1.62.0; atualize ambos na mesma mudança.

## Checklist operacional

- primeira execução manual concluída;
- controller e agente disponíveis durante a madrugada;
- fuso horário confirmado;
- causa **Started by timer** observada no teste temporário;
- concorrência desabilitada;
- JUnit e Allure publicados;
- screenshots, vídeos e traces arquivados;
- agenda final restaurada para `H 2 * * *`.
