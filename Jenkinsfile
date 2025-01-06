pipeline {
    agent any
 
    environment {
        BACKEND_IMAGE = "back-app"
        RABBITMQ_IMAGE = "rabbitmq:3-management"
        INVENTORY = "inventory.ini"
        PLAYBOOK = "deploy.yml"
        PROMETHEUS_PROCESS = "prometheus" // Prometheus process name
        PROMETHEUS_DIR = "/data/jenkins/prometheus" // Prometheus directory
        GRAFANA_CONTAINER = "grafana"
        LOKI_CONTAINER = "loki"
        MAVEN_OPTS = '-Dmaven.repo.local=/var/lib/jenkins/.m2/repository'
    }
 
    tools {
        maven 'Maven-3.8.5'
    }
 
    stages {
        stage('Clone Repository') {
            steps {
                //git branch: 'main', url: 'git@github.com:ahmedenzo/PinSenderBackend.git', credentialsId: '0fd2b45f-357b-4a7a-8e6a-968b10a9e78e'
                git branch: 'main', url: 'https://github.com/ahmedenzo/PinSenderBackend.git'
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
        stage('Check and Start Monitoring Services') {
            steps {
                script {
                    echo "Ensuring Prometheus, Grafana, and Loki are running..."
 
                    // Check and start Prometheus
                    def prometheusStatus = sh(script: "pgrep -f ${PROMETHEUS_PROCESS}", returnStatus: true)
                    if (prometheusStatus != 0) {
                        echo "Starting Prometheus..."
                        sh """
                            cd ${PROMETHEUS_DIR}
                            nohup ./prometheus --config.file=prometheus.yml &
                        """
                    } else {
                        echo "Prometheus is already running."
                    }
 
                    // Check and manage Grafana container
                    def grafanaRunning = sh(script: "docker ps --filter 'name=${GRAFANA_CONTAINER}' --filter 'status=running' -q", returnStdout: true).trim()
                    if (grafanaRunning) {
                        echo "Grafana container is already running: ${grafanaRunning}"
                    } else {
                        def grafanaExists = sh(script: "docker ps -a --filter 'name=${GRAFANA_CONTAINER}' -q", returnStdout: true).trim()
                        if (grafanaExists) {
                            echo "Removing existing Grafana container..."
                            sh "docker rm -f ${grafanaExists}"
                        }
                        echo "Starting Grafana container..."
                        sh """
                            docker run -d --name=${GRAFANA_CONTAINER} -p 3000:3000 grafana/grafana
                        """
                    }
 
                    // Check and manage Loki container
                    def lokiRunning = sh(script: "docker ps --filter 'name=${LOKI_CONTAINER}' --filter 'status=running' -q", returnStdout: true).trim()
                    if (lokiRunning) {
                        echo "Loki container is already running: ${lokiRunning}"
                    } else {
                        def lokiExists = sh(script: "docker ps -a --filter 'name=${LOKI_CONTAINER}' -q", returnStdout: true).trim()
                        if (lokiExists) {
                            echo "Removing existing Loki container..."
                            sh "docker rm -f ${lokiExists}"
                        }
                        echo "Starting Loki container..."
                        sh """
                            docker run -d --name=${LOKI_CONTAINER} -p 3100:3100 grafana/loki:latest
                        """
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
                dir('.') {
                    sh 'mvn clean package -DskipTests'
                }
            }
        }
 
        stage('Build Backend Docker Image') {
            steps {
                dir('.') {
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
                dir('.') {
                    sh '''
                        docker save -o backend-image.tar ${BACKEND_IMAGE}:latest
                        docker save -o rabbitmq-image.tar ${RABBITMQ_IMAGE}
                    '''
                }
            }
        }
 
        stage('Deploy with Ansible') {
            steps {
                dir('.') {
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