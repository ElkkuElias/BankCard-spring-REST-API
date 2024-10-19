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

            stage('Build Docker Image') {
                steps {
                    dir('backend') {
                        script {
                            // Logging in to Docker Hub
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
           }
    post {
        always {
            cleanWs()
        }
    }
}