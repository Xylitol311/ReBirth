# myproject/Dockerfile

FROM openjdk:17-jdk-alpine

# 작업 디렉토리 설정
WORKDIR /app

# Jenkins 빌드 후 생성된 JAR 복사
# (예: build/libs/rebirth-0.0.1-SNAPSHOT.jar 라고 가정)
COPY build/libs/*.jar app.jar

# 컨테이너 실행 시 스프링 부트 JAR 구동
ENTRYPOINT ["java", "-jar", "app.jar"]