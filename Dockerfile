FROM openjdk:17-jdk-alpine AS build

WORKDIR /app

COPY . .

RUN ./mvnw clean package -DskipTests

FROM openjdk:17-jdk-alpine

WORKDIR /app

COPY --from=build /app/target/Card-Service-0.0.1-SNAPSHOT.jar /app/app.jar

EXPOSE 9999

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

