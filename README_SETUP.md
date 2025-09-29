# Sistema de Chat Distribuído - Parte 1

Este projeto implementa a Parte 1 do sistema de chat distribuído usando o padrão Request-Reply.

## Arquitetura

- **Servidor**: Java (Spring Boot) na porta 8080
- **Cliente**: Go com interface CLI
- **Persistência**: JSON files armazenados em volume Docker
- **Orquestração**: Docker Compose

## Pré-requisitos

- Docker
- Docker Compose
- Git

## Estrutura do Projeto

```
distributed-systems-final-project/
├── part1/
│   ├── server/java/          # Servidor Java (Spring Boot)
│   │   ├── src/main/java/
│   │   │   └── com/distributedsystems/server/
│   │   │       ├── ServerApplication.java
│   │   │       ├── controller/ChatController.java
│   │   │       ├── model/
│   │   │       └── service/DataService.java
│   │   ├── src/main/resources/application.properties
│   │   ├── pom.xml
│   │   └── Dockerfile
│   ├── client/go/            # Cliente Go
│   │   ├── src/
│   │   │   ├── models/message.go
│   │   │   ├── client/client.go
│   │   │   ├── ui/cli.go
│   │   │   └── main.go
│   │   ├── go.mod
│   │   ├── go.sum
│   │   └── Dockerfile
│   ├── README.md             # Especificação original
│   └── README_IMPLEMENTACAO.md
├── docker-compose.yml
└── README_SETUP.md
```

## Funcionalidades Implementadas

### Servidor Java (Spring Boot)
1. **Login de usuário** - POST `/api/request` com service="login"
2. **Listagem de usuários** - POST `/api/request` com service="users"
3. **Criação de canal** - POST `/api/request` com service="channel"
4. **Listagem de canais** - POST `/api/request` com service="channels"

### Cliente Go
1. Interface CLI intuitiva
2. Login automático ao iniciar
3. Menu interativo com opções:
   - Listar usuários
   - Listar canais
   - Criar canal
   - Exit

## Como Executar

### Opção 1: Docker Compose (Recomendado)

1. **Build e iniciar os serviços:**
   ```bash
   docker-compose up --build
   ```

2. **Para testar com múltiplos clientes:**
   ```bash
   docker-compose --profile multi-client up --build
   ```

3. **Para executar em background:**
   ```bash
   docker-compose up -d --build
   ```

4. **Parar os serviços:**
   ```bash
   docker-compose down
   ```

### Opção 2: Execução Local (Desenvolvimento)

#### Servidor Java:

1. Entrar no diretório do servidor:
   ```bash
   cd part1/server/java
   ```

2. Compilar e executar:
   ```bash
   mvn clean package
   java -jar target/chat-server-1.0.0.jar
   ```

#### Cliente Go:

1. Entrar no diretório do cliente:
   ```bash
   cd part1/client/go
   ```

2. Baixar dependências:
   ```bash
   go mod download
   ```

3. Executar:
   ```bash
   go run src/main.go
   ```

## URLs e Endpoints

- **Servidor**: http://localhost:8080
- **Endpoint único**: POST http://localhost:8080/api/request

## Formatos de Mensagem (Request-Reply)

### Login
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

### Listar Usuários
```json
// Request
{
  "service": "users",
  "data": {
    "timestamp": "1034-01-15T10:30:00"
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

### Criar Canal
```json
// Request
{
  "service": "channel",
  "data": {
    "channel": "nome_do_canal",
    "timestamp": "2024-01-15T10:30:00"
  }
}

// Response
{
  "service": "channel",
  "data": {
    "status": "sucesso",
    "timestamp": "2024-01-15T10:30:01"
  }
}
```

### Listar Canais
```json
// Request
{
  "service": "channels",
  "data": {
    "timestamp": "2024-01-15T10:30:00"
  }
}

// Response
{
  "service": "channels",
  "data": {
    "timestamp": "2024-01-15T10:30:01",
    "users": ["canal1", "canal2"]
  }
}
```

## Persistência

- Os dados são salvos em arquivos JSON dentro do container
- Volume Docker montado em `/app/data/`
- Arquivos:
  - `users.json` - Lista de usuários cadastrados
  - `channels.json` - Lista de canais criados
- Dados persistem entre reinicializações do container

## Troubleshooting

1. **Erro de conexão com servidor**: Verificar se o servidor está rodando na porta 8080
2. **Erro de build**: Executar `docker-compose down` e `docker-compose up --build`
3. **Dados perdidos**: Os dados ficam armazenados no volume `chat-data`

## Logs

Para ver os logs dos serviços:
```bash
# Todos os logs
docker-compose logs

# Logs específicos
docker-compose logs chat-server
docker-compose logs chat-client-1
```

## Ambiente de Desenvolvimento

Para desenvolvimento local sem Docker, certifique-se de ter:
- Java 17+
- Maven 3.6+
- Go 1.21+

Configure a variável de ambiente `SERVER_URL` para o cliente Go apontar para o servidor Java.
