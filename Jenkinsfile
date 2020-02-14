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
                step([
                    $class           : 'JacocoPublisher',
                    execPattern      : 'build/jacoco/jacocoTest.exec',
                    classPattern     : 'build/classes/main',
                    sourcePattern    : 'src/main/java',
                    exclusionPattern : '**/*Test.class'
                ])
            }
        }
    }

}
