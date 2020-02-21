pipeline {

    agent any

    stages {
        stage ('Checkout') {
            steps {
                checkout scm
                sh 'ls -lat'
            }
        }
        stage ('Build') {
            steps {
                sh './gradlew clean build'
            }
        }
        stage ('Analysis') {
            steps {
                sh './gradlew check'
                junit "**/build/test-results/test/*.xml"
                jacoco(
                    execPattern: 'build/jacoco/jacoco.exec'
                )
            }
        }
        stage ('Docker') {
            steps {
                sh './gradlew docker'
            }
        }
    }

}
