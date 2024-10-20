pipeline {
    agent any

    environment {
        DOCKERHUB_CREDENTIALS_ID = 'dockerhub'
        DOCKERHUB_REPO = 'elkkuelias/springrest'
        DOCKER_IMAGE_TAG = 'latest'
    }
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

            }
        }

            stage('Build Docker Image') {
                steps {

                        script {

                            docker.withRegistry('https://index.docker.io/v1/', DOCKERHUB_CREDENTIALS_ID) {
                                // Build Docker image
                                def customImage = docker.build("${DOCKERHUB_REPO}:${DOCKER_IMAGE_TAG}")
                                // Push Docker image to Docker Hub
                                customImage.push()

                        }
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