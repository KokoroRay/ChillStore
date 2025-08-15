FROM openjdk:17-jdk-slim

RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

RUN ./mvnw dependency:go-offline -B

COPY src ./src

RUN ./mvnw clean package -DskipTests

RUN addgroup --system spring && adduser --system spring --ingroup spring
USER spring:spring

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=120s --retries=5 \
  CMD curl -f http://localhost:8080/ || exit 1

ENV SPRING_PROFILES_ACTIVE=docker
ENV JAVA_OPTS="-Xmx512m -Xms256m"

VOLUME ["/app/uploads"]
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar target/chillStore-0.0.1-SNAPSHOT.jar"]

