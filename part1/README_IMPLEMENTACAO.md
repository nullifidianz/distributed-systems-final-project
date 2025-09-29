# Parte 1: Implementação Completa - Request-Reply

Esta é a implementação completa da Parte 1 do projeto de sistemas distribuídos, desenvolvida usando o padrão Request-Reply.

## Arquitetura

- **Servidor**: Implementado em Java (Spring Boot)
- **Cliente**: Implementado em Go com interface CLI
- **Comunicação**: HTTP/REST com mensagens JSON
- **Persistência**: Arquivos JSON
- **Orquestração**: Docker Compose

## Estrutura do Projeto

```
part1/
├── server/java/              # Servidor Java (Spring Boot)
│   ├── src/main/java/com/distributedsystems/server/
│   │   ├── ServerApplication.java
│   │   ├── controller/ChatController.java
│   │   ├── model/ (RequestMessage, ResponseMessage, User, Channel)
│   │   └── service/DataService.java
│   ├── src/main/resources/application.properties
│   ├── pom.xml
│   └── Dockerfile
├── client/go/                # Cliente Go
│   ├── src/
│   │   ├── models/message.go
│   │   ├── client/client.go
│   │   ├── ui/cli.go
│   │   └── main.go
│   ├── go.mod
│   ├── go.sum
│   └── Dockerfile
├── README.md                # Especificação original
└── README_IMPLEMENTACAO.md  # Este arquivo
```

## Funcionalidades Implementadas

### 🔐 Login do Usuário
- Registro simples apenas com nome de usuário
- Validação de usuários duplicados
- Timestamp de quando foi realizado o login

### 👥 Listagem de Usuários
- Lista todos os usuários cadastrados no sistema
- Permite descobrir outros usuários conectados

### 📢 Criação de Canais
- Criação de novos canais para mensagens
- Validação de nomes de canais
- Prevenção de canais duplicados

### 📋 Listagem de Canais
- Lista todos os canais disponíveis
- Permite descobrir canais existentes

### 💾 Persistência de Dados
- Armazenamento em arquivos JSON
- Dados persistem entre reinicializações
- Volume Docker para persistência

## Como Executar

### Usando Docker Compose (Recomendado)

1. **Na raiz do projeto, execute:**
   ```bash
   docker-compose up --build
   ```

2. **Para usar o cliente interativo:**
   ```bash
   # Em uma nova sessão de terminal
   docker exec -it distributed-chat-client-1 ./main
   ```

3. **Para teste com múltiplos clientes:**
   ```bash
   docker-compose --profile multi-client up --build
   ```

### Executando Separadamente

#### Servidor Java:
```bash
cd part1/server/java
mvn clean package
java -jar target/chat-server-1.0.0.jar
```

#### Cliente Go:
```bash
cd part1/client/go
go mod download
go run src/main.go
```

## Protocolo de Comunicação

O protocolo segue exatamente a especificação original, usando mensagens JSON com formato Request-Reply.

### Endpoint Único
- **URL**: `POST http://localhost:8080/api/request`
- **Content-Type**: `application/json`

### Mensagens de Login
```json
// Request
{
  "service": "login",
  "data": {
    "user": "nome_do_usuario",
    "timestamp": "2024-01-15T10:30:00"
  }
}

// Response
{
  "service": "login",
  "data": {
    "status": "sucesso",
    "timestamp": "2024-01-15T10:30:01"
  }
}
```

### Mensagens de Listagem de Usuários
```json
// Request
{
  "service": "users",
  "data": {
    "timestamp": "2024-01-15T10:30:00"
  }
}

// Response
{
  "service": "users",
  "data": {
    "timestamp": "2024-01-15T10:30:01",
    "users": ["usuario1", "usuario2"]
  }
}
```

### Mensagens de Canal
```json
// Request (Criar)
{
  "service": "channel",
  "data": {
    "channel": "nome_do_canal",
    "timestamp": "2024-01-15T10:30:00"
  }
}

// Response (Criar)
{
  "service": "channel",
  "data": {
    "status": "sucesso",
    "timestamp": "2024-01-15T10:30:01"
  }
}

// Request (Listar)
{
  "service": "channels",
  "data": {
    "timestamp": "2024-01-15T10:30:00"
  }
}

// Response (Listar)
{
  "service": "channels",
  "data": {
    "timestamp": "2024-01-15T10:30:01",
    "users": ["canal1", "canal2"]
  }
}
```

## Testando o Sistema

Execute o script de teste na raiz do projeto:
```bash
./test-system.sh
```

Este script fará testes automatizados de todas as funcionalidades implementadas.

## Próximas Partes

Na Parte 2, serão implementadas:
- Troca de mensagens entre usuários
- Publicação de mensagens em canais
- Mensagens persistentes
