pipeline {
  agent {
    docker {
      image 'mcr.microsoft.com/playwright/java:v1.62.0-noble'
      args '--init --ipc=host'
      reuseNode true
    }
  }
  options {
    timestamps()
    disableConcurrentBuilds()
    buildDiscarder(logRotator(numToKeepStr: '20', artifactNumToKeepStr: '10'))
  }
  triggers {
    // Uma vez por dia, em um minuto estável entre 02:00 e 02:59.
    // O horário usa o fuso configurado no controller Jenkins.
    cron('H 2 * * *')
  }
  environment {
    COURSE_HEADLESS = 'true'
    COURSE_TRACE = 'on-failure'
    COURSE_SCREENSHOT = 'on-failure'
    COURSE_VIDEO = 'off'
    COURSE_ARTIFACTS_DIR = 'artifacts'
  }
  stages {
    stage('Compile') {
      steps { sh 'mvn -B -ntp test-compile' }
    }
    stage('Validators') {
      steps {
        sh 'mvn -B -ntp -Dtest=ArchitectureValidator test'
        sh 'mvn -B -ntp -Dtest=CourseSourceValidator -Dcourse.module=12 test'
      }
    }
    stage('Smoke cross-browser') {
      steps {
        // Sequencial no mesmo workspace: evita três processos Maven escrevendo no mesmo target.
        sh 'mvn -B -ntp -Dtest=*ExercicioTest -Dgroups=smoke -Dbrowser=chromium test'
        sh 'mvn -B -ntp -Dtest=*ExercicioTest -Dgroups=smoke -Dbrowser=firefox test'
        sh 'mvn -B -ntp -Dtest=*ExercicioTest -Dgroups=smoke -Dbrowser=webkit test'
      }
    }
    stage('Regression Chromium') {
      steps { sh 'mvn -B -ntp -Dtest=*ExercicioTest -Dgroups=regression -Dbrowser=chromium test' }
    }
  }
  post {
    always {
      junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
      allure includeProperties: false, jdk: '', results: [[path: 'target/allure-results']]
      archiveArtifacts allowEmptyArchive: true,
        artifacts: 'artifacts/**/*,target/allure-results/**/*,target/surefire-reports/**/*'
    }
  }
}
