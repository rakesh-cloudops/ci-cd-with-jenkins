```groovy
pipeline {
    agent {
        label 'k3s-worker'
    }

    options {
        timestamps()
        disableConcurrentBuilds()
        timeout(time: 30, unit: 'MINUTES')
    }

    parameters {
        booleanParam(
            name: 'ANALYZE',
            defaultValue: false,
            description: 'Run SonarQube and wait for the quality gate.'
        )

        booleanParam(
            name: 'PUBLISH',
            defaultValue: false,
            description: 'Build the image, push it, and deploy to Kubernetes.'
        )

        string(
            name: 'IMAGE',
            defaultValue: 'example.com/my-app',
            description: 'Image name without a tag. BUILD_NUMBER will be used as the tag.'
        )

        string(
            name: 'REGISTRY',
            defaultValue: 'https://index.docker.io/v1/',
            description: 'Docker registry URL.'
        )
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Test') {
            steps {
                sh '''
                    whoami
                    mvn -B verify
                '''
            }
        }

        stage('SonarQube') {
            when {
                expression {
                    return params.ANALYZE == true
                }
            }

            steps {
                withSonarQubeEnv('SonarQube') {
                    sh '''
                        mvn -B org.sonarsource.scanner.maven:sonar-maven-plugin:3.11.0.3922:sonar
                    '''
                }
            }
        }

        stage('Quality Gate') {
            when {
                expression {
                    return params.ANALYZE == true
                }
            }

            steps {
                timeout(time: 10, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Docker Build') {
            when {
                expression {
                    return params.PUBLISH == true
                }
            }

            steps {
                sh """
                    docker build -t ${params.IMAGE}:${env.BUILD_NUMBER} .
                """
            }
        }

        stage('Docker Login') {
            when {
                expression {
                    return params.PUBLISH == true
                }
            }

            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'registry-credentials',
                        usernameVariable: 'DOCKER_USERNAME',
                        passwordVariable: 'DOCKER_PASSWORD'
                    )
                ]) {
                    sh '''
                        echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin
                    '''
                }
            }
        }

        stage('Docker Push') {
            when {
                expression {
                    return params.PUBLISH == true
                }
            }

            steps {
                sh """
                    docker push ${params.IMAGE}:${env.BUILD_NUMBER}
                """
            }
        }

        stage('Deploy') {
            when {
                expression {
                    return params.PUBLISH == true
                }
            }

            steps {
                sh """
                    sed 's|__IMAGE__|${params.IMAGE}:${env.BUILD_NUMBER}|' k8s/deployment.yaml | kubectl apply -f -
                """

                sh '''
                    kubectl apply -f k8s/service.yaml
                '''
            }
        }
    }

    post {
        always {
            junit testResults: 'target/surefire-reports/*.xml'
        }
    }
}
```
