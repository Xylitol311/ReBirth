pipeline {
    agent any

    environment {
        COMPOSE_FILE = 'docker-compose.yml'
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
                echo 'Gradle 빌드 (테스트 생략)'
                sh './gradlew clean build -x test'
            }
        }
        stage('Docker Image Build') {
            steps {
                echo 'Docker 이미지 빌드 중...'
                // rebirth라는 이름으로 이미지를 빌드
                sh 'docker build -t rebirth-image -f Dockerfile .'
            }
        }
        stage('Deploy') {
            steps {
                echo 'Rebirth 컨테이너 재배포 중...'
                // --no-deps로 다른 서비스(nginx)는 건드리지 않고 rebirth만 재시작
                sh 'docker-compose up -d --no-deps --build rebirth'
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