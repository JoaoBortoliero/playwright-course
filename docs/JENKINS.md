# Jenkins, Docker e execução diária

## Pré-requisitos

O controller Jenkins precisa estar em execução e possuir um agente Linux com Docker disponível. Instale os plugins Pipeline, Git, Docker Pipeline, JUnit e Allure Jenkins Plugin.

O projeto utiliza um `Dockerfile` próprio, baseado na imagem Java do Playwright, que instala o Microsoft Edge Stable e o PowerShell. O Jenkins executa os testes pelo `course.ps1` dentro desse container.

Se o Jenkins estiver em uma máquina desligada durante a madrugada, a execução não ocorrerá. Para uma agenda confiável, mantenha o controller e pelo menos um agente compatível disponíveis no horário programado.

## Criar o job a partir do repositório

1. No Jenkins, selecione **New Item** e crie um job do tipo **Pipeline**.
2. Em **Pipeline**, escolha **Pipeline script from SCM**.
3. Selecione Git e informe a URL do repositório.
4. Configure a credencial quando necessária.
5. Informe a branch e mantenha `Jenkinsfile` como Script Path.
6. Salve e execute **Build Now** antes de confiar no agendamento.
7. Confirme os estágios, resultados JUnit, relatório Allure e artefatos.

O agente Docker do Jenkins usa o `Dockerfile` do repositório. O workspace é reutilizado pelo container com `reuseNode true`.

## Estágios esperados

```text
Setup -> Smoke Edge -> Regressão Edge -> Validação completa -> Publicação
```

A execução utiliza:

```powershell
pwsh -File ./course.ps1 setup
pwsh -File ./course.ps1 exercise 12 -Headless -Groups smoke
pwsh -File ./course.ps1 exercise 12 -Headless -Groups regression -Parallel
pwsh -File ./course.ps1 validate 12 -Headless
```

Não utilize `mvn` diretamente no `Jenkinsfile`. O `mvn-local.ps1` permanece uma implementação interna do `course.ps1`.

## Agenda

O `Jenkinsfile` contém:

```groovy
triggers {
    cron('H 2 * * *')
}
```

`H 2 * * *` solicita uma execução diária em um minuto estável entre 02:00 e 02:59. O horário segue o fuso do controller Jenkins.

`disableConcurrentBuilds()` impede duas execuções simultâneas da mesma pipeline.

Referência: [triggers e sintaxe cron do Jenkins](https://www.jenkins.io/doc/book/pipeline/syntax/#triggers).

## Exercício seguro do agendamento

Depois que uma execução manual estiver verde:

1. em uma branch de laboratório, altere temporariamente para `cron('H/5 * * * *')`;
2. publique a branch e aguarde o Jenkins carregar o novo `Jenkinsfile`;
3. confirme uma execução iniciada por timer;
4. valide relatórios e artefatos;
5. restaure `cron('H 2 * * *')` antes da integração.

## Segredos

Use Jenkins Credentials e limite cada segredo ao menor escopo necessário.

```groovy
withCredentials([usernamePassword(
    credentialsId: 'test-user',
    usernameVariable: 'TEST_USER',
    passwordVariable: 'TEST_PASSWORD'
)]) {
    sh 'pwsh -File ./course.ps1 exercise 12 -Headless'
}
```

Não imprima credenciais, tokens, cookies ou storage state. Não inclua segredos em screenshots, traces, vídeos ou anexos Allure.

## Publicação

O bloco `post { always { ... } }` deve publicar:

- `target/surefire-reports/*.xml`;
- `target/allure-results/**/*`;
- `artifacts/**/*`.

## Checklist operacional

- primeira execução manual concluída;
- somente Microsoft Edge utilizado;
- controller e agente disponíveis durante a madrugada;
- fuso confirmado;
- causa **Started by timer** observada no teste temporário;
- concorrência desabilitada;
- JUnit e Allure publicados;
- screenshots, vídeos e traces arquivados;
- agenda final restaurada para `H 2 * * *`.
