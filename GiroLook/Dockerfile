# 1. A Base (O Sistema Operacional + Java)
FROM eclipse-temurin:21-jre-jammy

# 2. O Diretório de Trabalho
WORKDIR /app

# 3. O Transporte (Copiando o seu JAR)
COPY target/GiroLook-0.0.1-SNAPSHOT.jar app.jar

# 4. A Porta de Comunicação
EXPOSE 8080

# 5. O Comando de Partida
ENTRYPOINT ["java", "-jar", "app.jar"]