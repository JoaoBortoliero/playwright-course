pipeline {
  agent {
    dockerfile {
      filename 'Dockerfile'
      args '--init --ipc=host'
      reuseNode true
    }
  }

  options {
    timestamps()
    disableConcurrentBuilds()
    buildDiscarder(logRotator(
      numToKeepStr: '20',
      artifactNumToKeepStr: '10'
    ))
  }

  triggers {
    // Uma vez por dia, em um minuto estavel entre 02:00 e 02:59.
    // O horario usa o fuso configurado no controller Jenkins.
    cron('H 2 * * *')
  }

  environment {
    COURSE_ARTIFACTS_DIR = 'artifacts'
  }

  stages {
    stage('Setup') {
      steps {
        sh 'pwsh -File ./course.ps1 setup'
      }
    }

    stage('Smoke Edge') {
      steps {
        sh 'pwsh -File ./course.ps1 exercise 12 -Headless -Groups smoke -Trace on-failure -Screenshot on-failure -Video off'
      }
    }

    stage('Regression Edge') {
      steps {
        sh 'pwsh -File ./course.ps1 exercise 12 -Headless -Groups regression -Parallel -Trace on-failure -Screenshot on-failure -Video off'
      }
    }

    stage('Validacao completa') {
      steps {
        // Executa o exercicio da aula, CourseSourceValidator e ArchitectureValidator.
        sh 'pwsh -File ./course.ps1 validate 12 -Headless -Trace on-failure -Screenshot on-failure -Video off'
      }
    }
  }

  post {
    always {
      junit(
        allowEmptyResults: true,
        testResults: '**/target/surefire-reports/*.xml'
      )

      allure(
        includeProperties: false,
        jdk: '',
        results: [[path: 'target/allure-results']]
      )

      archiveArtifacts(
        allowEmptyArchive: true,
        artifacts: 'artifacts/**/*,target/allure-results/**/*,target/surefire-reports/**/*'
      )
    }
  }
}
