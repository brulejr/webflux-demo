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
            post {
                always {
                    junit "**/build/test-results/*.xml"
                    step([
                        $class         : 'FindBugsPublisher',
                        pattern        : 'build/reports/findbugs/*.xml',
                        canRunOnFailed : true
                    ])
                    step([
                        $class         : 'PmdPublisher',
                        pattern        : 'build/reports/pmd/*.xml',
                        canRunOnFailed : true
                    ])
                    step([
                        $class           : 'JacocoPublisher',
                        execPattern      : 'build/jacoco/jacoco.exec',
                        classPattern     : 'build/classes/main',
                        sourcePattern    : 'src/main/java',
                        exclusionPattern : '**/*Test.class'
                    ])
                    publishHTML([
                        allowMissing          : false,
                        alwaysLinkToLastBuild : false,
                        keepAll               : true,
                        reportDir             : 'build/asciidoc/html5',
                        reportFiles           : 'index.html',
                        reportTitles          : "API Documentation",
                        reportName            : "API Documentation"
                    ])
                    sh "gradle clean"
                }
            }
        }
    }

}
