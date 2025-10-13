# Parte 2: Implementação Completa - Publisher-Subscriber

Esta é a implementação completa da Parte 2 do projeto de sistemas distribuídos, desenvolvida usando o padrão Publisher-Subscriber com ZeroMQ.

## Arquitetura

- **Servidor**: Java (Spring Boot) com ZeroMQ REP na porta 5555
- **Cliente**: Go com ZeroMQ REQ/SUB e interface CLI
- **Proxy Pub/Sub**: Python com XSUB (5557) e XPUB (5558)
- **Bot Automatizado**: Python que publica mensagens automaticamente
- **Comunicação**: ZeroMQ para todas as operações
- **Persistência**: Arquivos JSON para usuários, canais, mensagens e publicações
- **Orquestração**: Docker Compose

## Estrutura do Projeto

```
part2/
├── proxy/python/              # Proxy Pub/Sub Python
│   ├── proxy.py
│   ├── requirements.txt
│   └── Dockerfile
├── client/python-bot/         # Cliente Bot Automatizado
│   ├── bot.py
│   ├── requirements.txt
│   └── Dockerfile
└── README_IMPLEMENTACAO.md    # Este arquivo

part1/ (refatorado)
├── server/java/               # Servidor com ZeroMQ
│   ├── src/.../server/ZmqServer.java (novo)
│   ├── src/.../service/PublisherService.java (novo)
│   └── pom.xml (adicionado JeroMQ)
└── client/go/                 # Cliente com ZeroMQ
    ├── src/client/client.go (refatorado)
    ├── src/ui/cli.go (atualizado)
    └── go.mod (adicionado zmq4)
```

## Funcionalidades Implementadas

### 🔄 Refatoração da Parte 1 para ZeroMQ
- **Servidor Java**: Substituído HTTP por ZeroMQ REP (porta 5555)
- **Cliente Go**: Substituído HTTP por ZeroMQ REQ/SUB
- **Mantidas**: Todas as funcionalidades da Parte 1 (login, listagem, criação de canais)

### 📢 Sistema Publisher-Subscriber
- **Proxy Python**: XSUB (5557) ↔ XPUB (5558) para roteamento de mensagens
- **Publicação em Canais**: Usuários podem publicar mensagens em canais
- **Mensagens Diretas**: Usuários podem enviar mensagens privadas
- **Subscrição**: Clientes se inscrevem automaticamente para receber mensagens

### 🤖 Cliente Bot Automatizado
- **Geração Automática**: Nomes de usuário aleatórios
- **Publicação Contínua**: Loop infinito publicando em canais aleatórios
- **Mensagens Pré-definidas**: 10 mensagens diferentes para variedade
- **Múltiplas Réplicas**: 2 bots rodando simultaneamente

### 💾 Persistência Expandida
- **Usuários**: `users.json` (mantido da Parte 1)
- **Canais**: `channels.json` (mantido da Parte 1)
- **Publicações**: `publications.json` (novo)
- **Mensagens**: `messages.json` (novo)

## Como Executar

### Opção 1: Docker Compose (Recomendado)

1. **Iniciar todos os serviços:**
   ```bash
   docker-compose up --build
   ```

2. **Para usar o cliente interativo:**
   ```bash
   # Em uma nova sessão de terminal
   docker exec -it distributed-chat-client-1 ./main
   ```

3. **Para testar com múltiplos clientes:**
   ```bash
   docker-compose --profile multi-client up --build
   ```

4. **Para testar com múltiplos bots:**
   ```bash
   docker-compose --profile multi-bot up --build
   ```

5. **Para executar em background:**
   ```bash
   docker-compose up -d --build
   ```

### Opção 2: Executar Separadamente

#### Proxy Pub/Sub:
```bash
cd part2/proxy/python
pip install -r requirements.txt
python proxy.py
```

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

#### Bot Python:
```bash
cd part2/client/python-bot
pip install -r requirements.txt
python bot.py
```

## Protocolo de Comunicação

### ZeroMQ Sockets
- **Servidor**: REP na porta 5555
- **Cliente REQ**: Conecta ao servidor (5555)
- **Cliente SUB**: Conecta ao proxy XPUB (5558)
- **Proxy XSUB**: Porta 5557 (recebe publicações)
- **Proxy XPUB**: Porta 5558 (distribui para subscribers)

### Mensagens REQ-REP (Mantidas da Parte 1)

#### Login
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

### Novas Mensagens REQ-REP

#### Publicar em Canal
```json
// Request
{
  "service": "publish",
  "data": {
    "user": "alice",
    "channel": "geral",
    "message": "Olá pessoal!",
    "timestamp": "2024-01-15T10:30:00"
  }
}

// Response
{
  "service": "publish",
  "data": {
    "status": "OK",
    "timestamp": "2024-01-15T10:30:01"
  }
}
```

#### Enviar Mensagem Direta
```json
// Request
{
  "service": "message",
  "data": {
    "src": "alice",
    "dst": "bob",
    "message": "Oi Bob, como vai?",
    "timestamp": "2024-01-15T10:30:00"
  }
}

// Response
{
  "service": "message",
  "data": {
    "status": "OK",
    "timestamp": "2024-01-15T10:30:01"
  }
}
```

### Mensagens PUB-SUB

#### Publicação em Canal
```
Tópico: "canal_geral"
Payload: {"user": "alice", "message": "Olá pessoal!", "timestamp": "2024-01-15T10:30:00"}
```

#### Mensagem Direta
```
Tópico: "user_bob"
Payload: {"src": "alice", "message": "Oi Bob, como vai?", "timestamp": "2024-01-15T10:30:00"}
```

## Interface do Cliente

### Menu Principal
```
══════════════════════════
     MENU PRINCIPAL
══════════════════════════
1. Listar usuários
2. Listar canais
3. Criar canal
4. Publicar em canal
5. Enviar mensagem direta
6. Inscrever em canal
7. Exit
══════════════════════════
```

### Funcionalidades
- **Login Automático**: Ao iniciar, solicita nome de usuário
- **Subscrição Automática**: Se inscreve automaticamente para mensagens diretas
- **Recebimento em Tempo Real**: Exibe mensagens recebidas instantaneamente
- **Publicação**: Permite publicar em canais existentes
- **Mensagens Diretas**: Envia mensagens para usuários específicos
- **Subscrição Manual**: Permite se inscrever em canais adicionais

## Cliente Bot Automatizado

### Características
- **Nomes Aleatórios**: Gera nomes como "RapidoBot123", "InteligenteAssistente456"
- **Ciclo de Mensagens**: Envia 10 mensagens por ciclo
- **Canais Aleatórios**: Escolhe canais aleatoriamente
- **Mensagens Variadas**: 10 mensagens pré-definidas diferentes
- **Intervalos**: Aguarda 2-5 segundos entre mensagens, 10-20 segundos entre ciclos
- **Criação de Canais**: Cria canais padrão se não existirem

### Mensagens do Bot
1. "Olá pessoal! Como vocês estão?"
2. "Alguém quer conversar sobre tecnologia?"
3. "Que dia lindo hoje!"
4. "Alguém tem alguma dica interessante?"
5. "Vamos discutir sobre sistemas distribuídos!"
6. "Que tal falarmos sobre ZeroMQ?"
7. "Alguém já usou Docker em produção?"
8. "Vamos compartilhar experiências!"
9. "Que tal um café virtual?"
10. "Alguém tem projetos interessantes para mostrar?"

## Testando o Sistema

### Script de Teste Automatizado
```bash
./test-pubsub.sh
```

### Teste Manual
1. **Iniciar sistema:**
   ```bash
   docker-compose up -d
   ```

2. **Acessar cliente:**
   ```bash
   docker exec -it distributed-chat-client-1 ./main
   ```

3. **Testar funcionalidades:**
   - Fazer login
   - Criar um canal
   - Publicar mensagem no canal
   - Inscrever em outro canal
   - Enviar mensagem direta

4. **Verificar bots:**
   ```bash
   docker-compose logs chat-bot-1 chat-bot-2
   ```

### Verificações
- ✅ Servidor REQ-REP respondendo na porta 5555
- ✅ Proxy XSUB funcionando na porta 5557
- ✅ Proxy XPUB funcionando na porta 5558
- ✅ Publicações sendo distribuídas corretamente
- ✅ Mensagens diretas chegando aos destinatários
- ✅ Bots publicando mensagens automaticamente
- ✅ Persistência funcionando (arquivos JSON)

## Logs e Monitoramento

### Ver Logs
```bash
# Todos os serviços
docker-compose logs

# Serviços específicos
docker-compose logs chat-server
docker-compose logs pubsub-proxy
docker-compose logs chat-bot-1
docker-compose logs chat-client-1
```

### Logs Importantes
- **Servidor**: Login, criação de canais, publicações, mensagens
- **Proxy**: Conexões, roteamento de mensagens
- **Bot**: Login, criação de canais, publicações automáticas
- **Cliente**: Conexões, mensagens recebidas

## Próximas Partes

Na Parte 3, serão implementadas:
- Sistema de autenticação mais robusto
- Criptografia de mensagens
- Histórico de mensagens
- Notificações push
- Interface web

## Troubleshooting

### Problemas Comuns

1. **Erro de conexão ZeroMQ:**
   - Verificar se as portas 5555, 5557, 5558 estão abertas
   - Verificar se o proxy está rodando antes do servidor

2. **Mensagens não chegando:**
   - Verificar se o cliente está inscrito no tópico correto
   - Verificar logs do proxy para roteamento

3. **Bot não publicando:**
   - Verificar se há canais disponíveis
   - Verificar logs do bot para erros

4. **Dados perdidos:**
   - Verificar volume Docker `chat-data`
   - Verificar permissões de escrita no diretório `/app/data`

### Comandos Úteis
```bash
# Rebuild completo
docker-compose down
docker-compose up --build

# Limpar volumes
docker-compose down -v

# Ver status dos containers
docker-compose ps

# Entrar no container
docker exec -it distributed-chat-server bash
```
