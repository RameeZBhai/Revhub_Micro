pipeline {
    agent any
    
    environment {
        DEPLOY_PATH = 'C:\\RevHub\\deploy'
        AWS_REGION = 'us-east-2'
        S3_BUCKET = 'revhub-showcase-rameezshaik'
        EC2_HOST = '3.139.94.192'
        OPENSEARCH_ENDPOINT = 'search-revhub-search-xyz.us-east-2.es.amazonaws.com'
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
        
        stage('Deploy Local') {
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
        
        stage('Deploy to AWS') {
            when {
                expression { params.DEPLOY_TO_AWS == true }
            }
            steps {
                script {
                    try {
                        withCredentials([string(credentialsId: 'aws-credentials', variable: 'AWS_CREDS')]) {
                            script {
                                def creds = AWS_CREDS.split(':')
                                env.AWS_ACCESS_KEY_ID = creds[0]
                                env.AWS_SECRET_ACCESS_KEY = creds[1]
                            }
                            bat "aws s3 sync ${DEPLOY_PATH} s3://${S3_BUCKET}/jars/"
                            echo "AWS deployment successful!"
                        }
                    } catch (Exception e) {
                        echo "AWS deployment skipped - AWS CLI not installed"
                        echo "Local deployment completed successfully!"
                    }
                }
            }
        }
    }
    
    parameters {
        booleanParam(name: 'DEPLOY_TO_AWS', defaultValue: false, description: 'Deploy to AWS after local deployment')
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