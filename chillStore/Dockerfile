# ==========================================
# STAGE 1: Build
# ==========================================
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

RUN sed -i 's/\r$//' mvnw && chmod +x mvnw && ./mvnw dependency:go-offline -B

COPY src ./src
RUN ./mvnw clean package -DskipTests

# ==========================================
# STAGE 2: Runtime (Tối ưu RAM và kích thước Image)
# ==========================================
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

RUN groupadd -r spring && useradd -r -g spring spring \
    && mkdir -p /app/uploads \
    && chown -R spring:spring /app

COPY --from=build --chown=spring:spring /app/target/chillStore-*.jar app.jar

USER spring:spring

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_TOOL_OPTIONS="-XX:+TieredCompilation -XX:TieredStopAtLevel=1 -Xms32m -Xmx96m -XX:MaxMetaspaceSize=64m -XX:+UseSerialGC -Dspring.main.lazy-initialization=true"

VOLUME ["/app/uploads"]
ENTRYPOINT ["sh", "-c", "exec java -jar app.jar"]
