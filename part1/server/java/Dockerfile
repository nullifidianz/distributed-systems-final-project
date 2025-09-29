FROM openjdk:17-jdk-slim

# Criar diretório de trabalho
WORKDIR /app

# Copiar arquivos de configuração Maven
COPY pom.xml .

# Copiar código fonte
COPY src ./src

# Instalar Maven
RUN apt-get update && \
    apt-get install -y maven && \
    rm -rf /var/lib/apt/lists/*

# Build da aplicação
RUN mvn clean package -DskipTests

# Expor porta
EXPOSE 8080

# Criar diretório para dados
RUN mkdir -p /app/data

# Comando para executar a aplicação
CMD ["java", "-jar", "target/chat-server-1.0.0.jar"]
