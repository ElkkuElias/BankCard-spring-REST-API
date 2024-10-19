pipeline {
    agent any

    tools {
        gradle 'Gradle'
        jdk 'JDK21'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                bat 'gradlew.bat clean build'
            }
        }

        stage('Test') {
            steps {
                bat 'gradlew.bat test'
            }

        }



        stage('Package') {
            steps {
                bat 'gradlew.bat bootJar'
            }
        }

        stage('Deploy') {
            steps {
                echo 'Deploying...'
                // Add your deployment steps here
            }
        }
    }

    post {
        always {
            cleanWs()
        }
    }
}