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
                sh 'gradle clean build'
            }
        }

        stage('Test') {
            steps {
                sh 'gradle test'
            }
            post {
                always {
                    junit '**/build/test-results/test/*.xml'
                }
            }
        }

        stage('Code Coverage') {
            steps {
                sh 'gradle jacocoTestReport'
                jacoco(
                    execPattern: '**/build/jacoco/*.exec',
                    classPattern: '**/build/classes/java/main',
                    sourcePattern: '**/src/main/java'
                )
            }
        }

        stage('Static Code Analysis') {
            steps {
                sh 'gradle sonarqube'
            }
        }

        stage('Package') {
            steps {
                sh 'gradle bootJar'
            }
        }

        stage('Deploy') {
            steps {
            echo 'Deploying the application'
            }
        }
    }

    post {
        always {
            cleanWs()
        }
    }
}