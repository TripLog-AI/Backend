# ─── build stage ─────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace

# Maven wrapper + 의존성 캐시
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN ./mvnw -B -q dependency:go-offline

# 소스 복사 후 패키징
COPY src src
RUN ./mvnw -B -q -DskipTests package

# ─── runtime stage ───────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring

COPY --from=build /workspace/target/*.jar app.jar
RUN chown spring:spring app.jar

USER spring
EXPOSE 8080

# 컨테이너 환경에 맞춘 JVM 메모리 자동 조정
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
