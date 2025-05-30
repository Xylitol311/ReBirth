pipeline {
    agent any

    environment {
        COMPOSE_FILE = 'docker-compose.app.yml'
    }

    stages {
        stage('Checkout') {
            steps {
                echo '코드 체크아웃 중...'
                checkout scm
            }
        }
        stage('Build') {
            steps {
                echo 'Rebirth 모듈 빌드 (테스트 생략)'
                // rebirth 모듈 빌드
                dir('BE/rebirth') {
                    sh 'chmod +x gradlew'
                    sh './gradlew clean build -x test'
                }
                echo 'Cardissuer 모듈 빌드 (테스트 생략)'
                // cardissuer 모듈 빌드
                dir('BE/cardissuer') {
                    sh 'chmod +x gradlew'
                    sh './gradlew clean build -x test'
                }
                echo 'Bank 모듈 빌드 (테스트 생략)'
                // bank 모듈 빌드
                dir('BE/bank') {
                    sh 'chmod +x gradlew'
                    sh './gradlew clean build -x test'
                }
            }
        }
        stage('Docker Image Build') {
            steps {
                echo 'Docker 이미지 빌드 중...'
                // rebirth 이미지 빌드
                dir('BE/rebirth') {
                    sh 'docker build -t rebirth-image .'
                }
                // cardissuer 이미지 빌드
                dir('BE/cardissuer') {
                    sh 'docker build -t cardissuer-image .'
                }
                // bank 이미지 빌드
                dir('BE/bank') {
                    sh 'docker build -t bank-image .'
                }
            }
        }
        stage('Deploy') {
            steps {
                echo '애플리케이션 서비스 배포 중...'
                echo '기존 애플리케이션 컨테이너 종료 및 제거...'

                // 남아 있는 컨테이너 강제 제거 (이름 충돌 방지)
                sh '''
                docker ps -a --filter "name=-app" --format "{{.Names}}" | xargs -r docker rm -f || true
                '''
                // 네트워크나 orphan 컨테이너 포함 제거
                sh 'docker-compose -f docker-compose.app.yml down --remove-orphans || true'

                echo '변경된 설정으로 컨테이너 생성...'
                sh 'docker-compose -f docker-compose.app.yml up -d'
                echo 'nginx reload 실행...'
                sh 'docker exec -i nginx_proxy nginx -s reload'
            }
        }
    }
    post {
        success {
            echo '배포가 성공적으로 완료되었습니다.'
        }
        failure {
            echo '배포에 실패하였습니다. 로그를 확인하세요.'
        }
    }
}