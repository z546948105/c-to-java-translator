FROM openjdk:8-jdk-alpine AS builder

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN apk add --no-cache maven && \
    mvn clean package -DskipTests -q

FROM openjdk:8-jre-alpine

WORKDIR /app

COPY --from=builder /app/target/c-to-java-translator-1.0.0.jar app.jar

EXPOSE 9988

ENTRYPOINT ["java", "-jar", "app.jar"]
