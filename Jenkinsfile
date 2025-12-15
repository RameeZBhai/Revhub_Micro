pipeline {
    agent any
    
    environment {
        DEPLOY_PATH = 'C:\\RevHub\\deploy'
    }
    
    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/RameeZBhai/Revhub_Micro.git'
            }
        }
        
        stage('Build Backend') {
            steps {
                script {
                    def services = ['api-gateway', 'auth-service', 'user-service', 'post-service', 
                                  'chat-service', 'follow-service', 'notification-service', 'search-service']
                    
                    services.each { service ->
                        dir("microservices/${service}") {
                            bat 'mvn clean package -DskipTests'
                        }
                    }
                }
            }
        }
        
        stage('Build Frontend') {
            steps {
                dir('frontend') {
                    bat 'npm install'
                    bat 'ng build'
                }
            }
        }
        
        stage('Deploy') {
            steps {
                script {
                    bat "if not exist ${DEPLOY_PATH} mkdir ${DEPLOY_PATH}"
                    
                    def services = ['api-gateway', 'auth-service', 'user-service', 'post-service', 
                                  'chat-service', 'follow-service', 'notification-service', 'search-service']
                    
                    services.each { service ->
                        bat "copy microservices\\${service}\\target\\*.jar ${DEPLOY_PATH}\\"
                    }
                    
                    bat "start /B java -jar ${DEPLOY_PATH}\\api-gateway-1.0.0.jar"
                    sleep 10
                    bat "start /B java -jar ${DEPLOY_PATH}\\auth-service-1.0.0.jar"
                    bat "start /B java -jar ${DEPLOY_PATH}\\user-service-1.0.0.jar"
                    bat "start /B java -jar ${DEPLOY_PATH}\\post-service-1.0.0.jar"
                    bat "start /B java -jar ${DEPLOY_PATH}\\chat-service-1.0.0.jar"
                    bat "start /B java -jar ${DEPLOY_PATH}\\follow-service-1.0.0.jar"
                    bat "start /B java -jar ${DEPLOY_PATH}\\notification-service-1.0.0.jar"
                    bat "start /B java -jar ${DEPLOY_PATH}\\search-service-1.0.0.jar"
                }
            }
        }
    }
    
    post {
        success {
            echo 'Deployment successful!'
        }
        failure {
            echo 'Deployment failed!'
        }
    }
}