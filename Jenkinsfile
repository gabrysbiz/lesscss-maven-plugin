pipeline {
    agent any
    options {
        buildDiscarder(logRotator(artifactDaysToKeepStr: '-1', artifactNumToKeepStr: '10', daysToKeepStr: '-1', numToKeepStr: '10'))
        timestamps()
    }
    tools {
        // withMaven ignores tools: https://issues.jenkins-ci.org/browse/JENKINS-43651
        maven 'MVN-3'
        jdk 'JDK-9'
    }
    environment {
        MAVEN_ARGS = '-e -Dmaven.repo.local=.repository'
    }
    stages {
        stage('Build') {
            steps {
                withMaven(maven: 'MVN-3', jdk: 'JDK-9', publisherStrategy: 'EXPLICIT', options: [
                    artifactsPublisher(disabled: false), dependenciesFingerprintPublisher(disabled: false), openTasksPublisher(disabled: false)
                ]) {
                    sh "mvn ${MAVEN_ARGS} package -DskipTests"
                }
            }
        }
        stage('Verify') {
            steps {
                withMaven(maven: 'MVN-3', jdk: 'JDK-9', publisherStrategy: 'EXPLICIT', options: [junitPublisher(disabled: false)]) {
                    sh "mvn ${MAVEN_ARGS} verify"
                }
            }
        }
        stage('Build Docs') {
            steps {
                withMaven(maven: 'MVN-3', jdk: 'JDK-9', publisherStrategy: 'EXPLICIT') {
                    sh "mvn ${MAVEN_ARGS} site"
                }
            }
        }
    }
    post {
        always {
            cleanWs()
        }
    }
}
