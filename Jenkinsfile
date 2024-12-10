pipeline {
    agent any

    environment {
        BACKEND_IMAGE = "back-app"
        RABBITMQ_IMAGE = "rabbitmq:3-management"
        INVENTORY = "PinSenderBackend-main/inventory.ini"
        PLAYBOOK = "PinSenderBackend-main/deploy.yml"
        MAVEN_OPTS = '-Dmaven.repo.local=/var/lib/jenkins/.m2/repository'
    }

    tools {
        maven 'Maven-3.8.5'
    }

    stages {
        stage('Clone Repository') {
            steps {
                git branch: 'master', url: 'https://github.com/ahmedenzo/pinesender-fullProject.git'
            }
        }

        stage('Verify Required Files') {
            steps {
                script {
                    echo "Verifying required files: ${INVENTORY} and ${PLAYBOOK}..."

                    if (!fileExists("${INVENTORY}")) {
                        error "The file ${INVENTORY} is missing. Stopping the pipeline."
                    } else {
                        echo "The file ${INVENTORY} is present."
                    }

                    if (!fileExists("${PLAYBOOK}")) {
                        error "The file ${PLAYBOOK} is missing. Stopping the pipeline."
                    } else {
                        echo "The file ${PLAYBOOK} is present."
                    }
                }
            }
        }

        stage('Test Ansible Connection') {
            steps {
                script {
                    def connectionStatus = sh(
                        script: '''
                            echo "Testing Ansible connection..."
                            ansible all -m ping -i ${INVENTORY}
                        ''',
                        returnStatus: true
                    )
                    if (connectionStatus != 0) {
                        error "Ansible connection test failed! Skipping pipeline."
                    } else {
                        echo "Ansible connection successful."
                    }
                }
            }
        }

        stage('Build with Maven') {
            steps {
                dir('PinSenderBackend-main') {
                    sh 'mvn clean package -DskipTests'
                }
            }
        }

        stage('Build Backend Docker Image') {
            steps {
                dir('PinSenderBackend-main') {
                    sh '''
                        docker build -t ${BACKEND_IMAGE}:latest .
                    '''
                }
            }
        }

        stage('Pull RabbitMQ Docker Image') {
            steps {
                sh '''
                    docker pull ${RABBITMQ_IMAGE}
                '''
            }
        }

        stage('Export Docker Images') {
            steps {
                dir('PinSenderBackend-main') {
                    sh '''
                        docker save -o backend-image.tar ${BACKEND_IMAGE}:latest
                        docker save -o rabbitmq-image.tar ${RABBITMQ_IMAGE}
                    '''
                }
            }
        }

        stage('Deploy with Ansible') {
            steps {
                dir('PinSenderBackend-main') {
                    script {
                        // Trigger Ansible playbook with clean_deploy=true
                        def cleanDeploy = true
                        sh """
                            ansible-playbook -i ${INVENTORY} ${PLAYBOOK} -e clean_deploy=${cleanDeploy}
                        """
                    }
                }
            }
        }

        stage('Post-clean Specific Docker Image') {
            steps {
                script {
                    echo "Removing built Docker images for Backend and RabbitMQ to prevent overlay issues..."
                    sh '''
                        # Remove the Backend image
                        docker images back-app:latest -q | xargs --no-run-if-empty docker rmi -f

                        # Remove the RabbitMQ image
                        docker images rabbitmq:3-management -q | xargs --no-run-if-empty docker rmi -f


                        # Clean up unused Docker resources
                        docker system prune -f --volumes
                    '''
                }
            }
        }
    }

    post {
        success {
            echo 'Pipeline succeeded! 🎉'
        }
        failure {
            echo 'Pipeline failed. ❌'
        }
    }
}