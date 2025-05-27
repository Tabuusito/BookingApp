
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .

# El flag -B es para "batch mode" (no interactivo).
# dependency:go-offline es bueno, o dependency:resolve si prefieres.
# Si tu proyecto tiene módulos, mvn dependency:resolve podría ser más simple.
RUN mvn dependency:go-offline -B

COPY src ./src

# -DskipTests para acelerar el build en Docker (asume que los tests se corren en otro lado, ej. CI).
RUN mvn package -B -DskipTests

FROM openjdk:21-jdk-slim

ENV SERVER_PORT 8080

WORKDIR /app

RUN groupadd --system spring && useradd --system --gid spring spring
USER spring

COPY --from=build /app/target/reservas-0.0.1-SNAPSHOT.jar app.jar

EXPOSE ${SERVER_PORT}

ENTRYPOINT ["java", "-Dserver.port=${SERVER_PORT}", "-jar", "app.jar"]