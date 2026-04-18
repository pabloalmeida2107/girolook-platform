# ESTÁGIO 1: Compilação (Onde o Maven trabalha)
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
# Copia o pom.xml e o código fonte para dentro da Railway
COPY . .
# Executa o build lá na nuvem para gerar o seu JAR
RUN mvn clean package -DskipTests

# ESTÁGIO 2: Execução (Onde o seu app roda)
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
# Pega o arquivo .jar que foi gerado no ESTÁGIO 1 (o estágio "build")
COPY --from=build /app/target/GiroLook-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]