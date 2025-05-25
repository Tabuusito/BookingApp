FROM openjdk:21-jdk-slim

# Argumentos para la configuración de la JVM
ARG JVM_OPTS_DEBUG="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
ARG JVM_OPTS_PROD=""
ARG APP_PORT=8080

# Variable de entorno para seleccionar el perfil de JVM (debug o prod)
ENV JVM_OPTS_PROFILE="debug"
# Variable de entorno para el puerto de la aplicación (usado por Spring Boot)
ENV SERVER_PORT=${APP_PORT}

WORKDIR /app

# Crear un usuario no root para ejecutar la aplicación (mejora la seguridad)
RUN groupadd --system spring && useradd --system --gid spring spring
USER spring

ARG JAR_FILE=target/reservas-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

# Exponer el puerto de la aplicación y el puerto de depuración
EXPOSE ${APP_PORT}
EXPOSE 5005

# Elige los argumentos JVM basados en JVM_OPTS_PROFILE
# Si JVM_OPTS_PROFILE es "debug", usa JVM_OPTS_DEBUG. Si no, usa JVM_OPTS_PROD.
ENTRYPOINT [ "sh", "-c", "java ${JVM_OPTS_PROFILE:-debug} == 'debug' ? \"${JVM_OPTS_DEBUG}\" : \"${JVM_OPTS_PROD}\" -Dserver.port=${SERVER_PORT} -jar app.jar" ]
# Explicación del ENTRYPOINT:
# - "sh -c": Permite ejecutar un comando shell con lógica condicional.
# - "java ... -jar app.jar": El comando base para ejecutar la aplicación.
# - "${JVM_OPTS_PROFILE:-debug} == 'debug' ? \"${JVM_OPTS_DEBUG}\" : \"${JVM_OPTS_PROD}\"":
#   - Si la variable de entorno JVM_OPTS_PROFILE está definida como "debug" (o si no está definida, por defecto es "debug" gracias a `:-debug`),
#     entonces se usarán los argumentos de JVM_OPTS_DEBUG.
#   - De lo contrario (si JVM_OPTS_PROFILE es, por ejemplo, "prod"), se usarán los argumentos de JVM_OPTS_PROD.
# - "-Dserver.port=${SERVER_PORT}": Pasa el puerto de la aplicación como una propiedad del sistema a Spring Boot.