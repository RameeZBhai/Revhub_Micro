pipeline {
    agent any
    
    tools {
        maven 'Maven-3.8'
        nodejs 'NodeJS-18'
        jdk 'JDK-17'
    }
    
    environment {
        DEPLOY_PATH = 'C:\\RevHub\\deploy'
        DB_PASSWORD = credentials('db-password')
    }
    
    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'your-repo-url'
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
                    bat 'ng build --configuration production'
                }
            }
        }
        
        stage('Stop Services') {
            steps {
                bat 'taskkill /F /IM java.exe || exit 0'
                bat 'timeout /t 5'
            }
        }
        
        stage('Deploy') {
            steps {
                script {
                    // Copy JARs to deployment directory
                    bat "if not exist ${DEPLOY_PATH} mkdir ${DEPLOY_PATH}"
                    
                    def services = ['api-gateway', 'auth-service', 'user-service', 'post-service', 
                                  'chat-service', 'follow-service', 'notification-service', 'search-service']
                    
                    services.each { service ->
                        bat "copy microservices\\${service}\\target\\*.jar ${DEPLOY_PATH}\\"
                    }
                    
                    // Copy frontend build
                    bat "xcopy /E /I /Y frontend\\dist\\* ${DEPLOY_PATH}\\frontend\\"
                    
                    // Start services
                    bat "start /B java -jar ${DEPLOY_PATH}\\api-gateway-*.jar"
                    bat "timeout /t 10"
                    bat "start /B java -jar ${DEPLOY_PATH}\\auth-service-*.jar"
                    bat "start /B java -jar ${DEPLOY_PATH}\\user-service-*.jar"
                    bat "start /B java -jar ${DEPLOY_PATH}\\post-service-*.jar"
                    bat "start /B java -jar ${DEPLOY_PATH}\\chat-service-*.jar"
                    bat "start /B java -jar ${DEPLOY_PATH}\\follow-service-*.jar"
                    bat "start /B java -jar ${DEPLOY_PATH}\\notification-service-*.jar"
                    bat "start /B java -jar ${DEPLOY_PATH}\\search-service-*.jar"
                }
            }
        }
    }
    
    post {
        always {
            cleanWs()
        }
        success {
            echo 'Deployment successful!'
        }
        failure {
            echo 'Deployment failed!'
        }
    }
}