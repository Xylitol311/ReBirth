# BE/rebirth/Dockerfile

FROM openjdk:17-jdk-alpine

WORKDIR /app

# rebirth 모듈 빌드 산출물(JAR 파일)을 복사
# 예를 들어, build/libs/rebirth-0.0.1-SNAPSHOT.jar 라고 가정합니다.
COPY build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]