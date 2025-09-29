#!/bin/bash

# Script de teste do Sistema de Chat Distribuído - Parte 1

echo "🚀 Iniciando teste do Sistema de Chat Distribuído"
echo "================================================="
echo

# Funções auxiliares
print_step() {
    echo "📋 PASSO $1: $2"
    echo "----------------------------------------"
}

print_success() {
    echo "✅ $1"
}

print_info() {
    echo "ℹ️  $1"
}

print_error() {
    echo "❌ $1"
}

# Passo 1: Build e inicialização
print_step "1" "Build e inicialização dos containers"
docker-compose up -d --build

if [ $? -ne 0 ]; then
    print_error "Falha ao inicializar containers"
    exit 1
fi

print_success "Containers inicializados"

# Passo 2: Aguardar servidor ficar disponível
print_step "2" "Aguardando servidor ficar disponível"
sleep 15

# Testar se servidor está respondendo
print_info "Testando conexão com servidor..."
response=$(curl -s -o /dev/null -w "%{http_code}" \
    -X POST http://localhost:8080/api/request \
    -H "Content-Type: application/json" \
    -d '{"service":"users","data":{"timestamp":"'$(date -Iseconds)'"}}')

if [ "$response" = "200" ]; then
    print_success "Servidor está respondendo corretamente"
else
    print_error "Servidor não está respondendo (HTTP $response)"
    echo "Verificando logs do servidor..."
    docker-compose logs chat-server
    exit 1
fi

# Passo 3: Teste de endpoints
print_step "3" "Testando endpoints do servidor"

# Teste 1: Login de usuário
print_info "Testando login de usuário..."
login_response=$(curl -s -X POST http://localhost:8080/api/request \
    -H "Content-Type: application/json" \
    -d '{
        "service": "login",
        "data": {
            "user": "usuario_teste",
            "timestamp": "'$(date -Iseconds)'"
        }
    }')

echo "Resposta do login: $login_response"

if echo "$login_response" | grep -q "sucesso"; then
    print_success "Login funcionando corretamente"
else
    print_error "Login não funcionou"
fi

# Teste 2: Listar usuários
print_info "Testando listagem de usuários..."
users_response=$(curl -s -X POST http://localhost:8080/api/request \
    -H "Content-Type: application/json" \
    -d '{
        "service": "users",
        "data": {
            "timestamp": "'$(date -Iseconds)'"
        }
    }')

echo "Resposta da listagem: $users_response"

if echo "$users_response" | grep -q "usuario_teste"; then
    print_success "Listagem de usuários funcionando"
else
    print_error "Listagem de usuários não funcionando"
fi

# Teste 3: Criar canal
print_info "Testando criação de canal..."
channel_response=$(curl -s -X POST http://localhost:8080/api/request \
    -H "Content-Type: application/json" \
    -d '{
        "service": "channel",
        "data": {
            "channel": "canal_teste",
            "timestamp": "'$(date -Iseconds)'"
        }
    }')

echo "Resposta da criação: $channel_response"

if echo "$channel_response" | grep -q "sucesso"; then
    print_success "Criação de canal funcionando"
else
    print_error "Criação de canal não funcionando"
fi

# Teste 4: Listar canais
print_info "Testando listagem de canais..."
channels_response=$(curl -s -X POST http://localhost:8080/api/request \
    -H "Content-Type: application/json" \
    -d '{
        "service": "channels",
        "data": {
            "timestamp": "'$(date -Iseconds)'"
        }
    }')

echo "Resposta da listagem de canais: $channels_response"

if echo "$channels_response" | grep -q "canal_teste"; then
    print_success "Listagem de canais funcionando"
else
    print_error "Listagem de canais não funcionando"
fi

# Passo 4: Demonstração interativa
print_step "4" "Demonstração interativa"

echo "📱 Abra uma nova terminal e execute:"
echo "   docker exec -it distributed-chat-client-1 ./main"
echo
echo "🎯 Ou execute com múltiplos clientes:"
echo "   docker-compose --profile multi-client up chat-client-2"
echo

print_info "Os dados ficam persistidos no volume 'chat-data'"
print_info "Para parar o sistema: docker-compose down"
print_info "Para ver logs: docker-compose logs"

echo
echo "🎉 Sistema testado com sucesso!"
echo "================================================="
