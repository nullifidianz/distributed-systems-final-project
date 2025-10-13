#!/bin/bash

# Script de teste para o sistema Pub/Sub
echo "=== Teste do Sistema Publisher-Subscriber ==="
echo

# Função para testar conexão ZeroMQ
test_zmq_connection() {
    local port=$1
    local service=$2
    
    echo "Testando conexão $service na porta $port..."
    
    python3 -c "
import zmq
import sys
try:
    ctx = zmq.Context()
    s = ctx.socket(zmq.REQ)
    s.setsockopt(zmq.RCVTIMEO, 5000)  # 5 segundos timeout
    s.connect('tcp://localhost:$port')
    s.send_string('{\"service\":\"users\",\"data\":{\"timestamp\":\"\"}}')
    response = s.recv_string()
    print('✓ $service respondendo corretamente')
    s.close()
    ctx.term()
except Exception as e:
    print('✗ Erro ao conectar com $service: ' + str(e))
    sys.exit(1)
"
    
    if [ $? -eq 0 ]; then
        echo "✓ $service está funcionando"
    else
        echo "✗ $service não está respondendo"
        return 1
    fi
}

# Função para testar proxy Pub/Sub
test_pubsub_proxy() {
    echo "Testando proxy Pub/Sub..."
    
    python3 -c "
import zmq
import time
import threading

def publisher():
    try:
        ctx = zmq.Context()
        pub = ctx.socket(zmq.PUB)
        pub.connect('tcp://localhost:5557')
        time.sleep(0.1)  # Aguardar conexão
        pub.send_multipart([b'test_topic', b'test_message'])
        pub.close()
        ctx.term()
    except Exception as e:
        print('Erro no publisher: ' + str(e))

def subscriber():
    try:
        ctx = zmq.Context()
        sub = ctx.socket(zmq.SUB)
        sub.connect('tcp://localhost:5558')
        sub.setsockopt(zmq.SUBSCRIBE, b'test_topic')
        sub.setsockopt(zmq.RCVTIMEO, 5000)
        
        topic = sub.recv()
        message = sub.recv()
        
        if topic == b'test_topic' and message == b'test_message':
            print('✓ Proxy Pub/Sub funcionando corretamente')
        else:
            print('✗ Proxy Pub/Sub com problema')
            
        sub.close()
        ctx.term()
    except Exception as e:
        print('✗ Erro no subscriber: ' + str(e))

# Executar teste
sub_thread = threading.Thread(target=subscriber)
sub_thread.start()
time.sleep(0.1)
publisher()
sub_thread.join()
"
}

# Verificar se os containers estão rodando
echo "Verificando containers..."
if ! docker-compose ps | grep -q "Up"; then
    echo "✗ Containers não estão rodando. Execute: docker-compose up -d"
    exit 1
fi

echo "✓ Containers estão rodando"
echo

# Testar conexões
echo "=== Testando Conexões ==="
test_zmq_connection 5555 "Servidor REQ-REP"
test_zmq_connection 5557 "Proxy XSUB"
test_zmq_connection 5558 "Proxy XPUB"

echo
echo "=== Testando Proxy Pub/Sub ==="
test_pubsub_proxy

echo
echo "=== Teste de Funcionalidades ==="

# Teste de login
echo "Testando login..."
python3 -c "
import zmq
import json

ctx = zmq.Context()
s = ctx.socket(zmq.REQ)
s.connect('tcp://localhost:5555')

# Teste de login
request = {
    'service': 'login',
    'data': {
        'user': 'test_user',
        'timestamp': '2024-01-01T00:00:00'
    }
}

s.send_string(json.dumps(request))
response = json.loads(s.recv_string())

if response.get('data', {}).get('status') == 'sucesso':
    print('✓ Login funcionando')
else:
    print('✗ Login com problema:', response)

s.close()
ctx.term()
"

# Teste de criação de canal
echo "Testando criação de canal..."
python3 -c "
import zmq
import json

ctx = zmq.Context()
s = ctx.socket(zmq.REQ)
s.connect('tcp://localhost:5555')

request = {
    'service': 'channel',
    'data': {
        'channel': 'test_channel',
        'timestamp': '2024-01-01T00:00:00'
    }
}

s.send_string(json.dumps(request))
response = json.loads(s.recv_string())

if response.get('data', {}).get('status') == 'sucesso':
    print('✓ Criação de canal funcionando')
else:
    print('✗ Criação de canal com problema:', response)

s.close()
ctx.term()
"

# Teste de publicação
echo "Testando publicação..."
python3 -c "
import zmq
import json

ctx = zmq.Context()
s = ctx.socket(zmq.REQ)
s.connect('tcp://localhost:5555')

request = {
    'service': 'publish',
    'data': {
        'user': 'test_user',
        'channel': 'test_channel',
        'message': 'Mensagem de teste',
        'timestamp': '2024-01-01T00:00:00'
    }
}

s.send_string(json.dumps(request))
response = json.loads(s.recv_string())

if response.get('data', {}).get('status') == 'OK':
    print('✓ Publicação funcionando')
else:
    print('✗ Publicação com problema:', response)

s.close()
ctx.term()
"

echo
echo "=== Teste Concluído ==="
echo "Para testar manualmente:"
echo "1. docker-compose up -d"
echo "2. docker exec -it distributed-chat-client-1 ./main"
echo "3. Use o menu para testar as funcionalidades"
echo
echo "Para ver logs dos bots:"
echo "docker-compose logs chat-bot-1 chat-bot-2"
