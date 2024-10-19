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
            post {
                always {
                    junit '**/build/test-results/test/*.xml'
                }
            }
        }

        stage('Code Coverage') {
            steps {
                bat 'gradlew.bat jacocoTestReport'
                jacoco(
                    execPattern: '**/build/jacoco/*.exec',
                    classPattern: '**/build/classes/java/main',
                    sourcePattern: '**/src/main/java'
                )
            }
        }

        stage('Static Code Analysis') {
            steps {
                bat 'gradlew.bat sonarqube'
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