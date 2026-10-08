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
        booleanParam(name: 'ANALYZE', defaultValue: false, description: 'Run SonarQube and wait for the quality gate. The server in Jenkins must be named SonarQube.')
        booleanParam(name: 'PUBLISH', defaultValue: false, description: 'Build the image, push it, and apply the manifests. Needs a Linux agent with Docker and kubectl.')
        string(name: 'IMAGE', defaultValue: 'example.com/my-app', description: 'Image name without a tag. The tag is BUILD_NUMBER.')
        string(name: 'REGISTRY', defaultValue: 'https://index.docker.io/v1/', description: 'Registry endpoint for docker.withRegistry.')
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Test') {
            steps {
                sh 'whoami && mvn -B verify'
            }
        }

        stage('SonarQube') {
            when { expression { return params.ANALYZE == true } }
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh 'mvn -B org.sonarsource.scanner.maven:sonar-maven-plugin:3.11.0.3922:sonar'
                }
            }
        }

        stage('Quality gate') {
            when { expression { return params.ANALYZE == true } }
            steps {
                timeout(time: 10, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Image') {
            when { expression { return params.PUBLISH == true } }
            steps {
                sh "docker build -t ${params.IMAGE}:${env.BUILD_NUMBER} ."
            }
        }

        stage('Push') {
            when { expression { return params.PUBLISH == true } }
            steps {
                script {
                    docker.withRegistry(params.REGISTRY, 'registry-credentials') {
                        docker.image("${params.IMAGE}:${env.BUILD_NUMBER}").push()
                    }
                }
            }
        }

        stage('Deploy') {
            when { expression { return params.PUBLISH == true } }
            steps {
                sh "sed 's|__IMAGE__|${params.IMAGE}:${env.BUILD_NUMBER}|' k8s/deployment.yaml | kubectl apply -f -"
                sh 'kubectl apply -f k8s/service.yaml'
            }
        }
    }

    post {
        always {
            junit testResults: 'target/surefire-reports/*.xml'
        }
    }
}
