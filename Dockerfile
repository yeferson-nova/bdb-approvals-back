# Etapa 1: compila el JAR dentro del contenedor (no dependes de target/ local)
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /src

# Copia wrapper y POM primero para cachear dependencias
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw
RUN ./mvnw -q -DskipTests dependency:go-offline

# Copia el código fuente y construye
COPY src src
RUN ./mvnw -q -DskipTests package

# Etapa 2: runtime liviano
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /src/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
