#!/usr/bin/env python3
"""
Cliente Bot Automatizado para Sistema de Chat Distribuído
Publica mensagens automaticamente em canais aleatórios
"""

import zmq
import json
import time
import random
import string
import sys
import signal
import logging
from datetime import datetime

# Configurar logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

class ChatBot:
    def __init__(self):
        self.context = None
        self.req_socket = None
        self.sub_socket = None
        self.username = None
        self.available_channels = []
        self.running = False
        
        # Mensagens pré-definidas para o bot
        self.messages = [
            "Olá pessoal! Como vocês estão?",
            "Alguém quer conversar sobre tecnologia?",
            "Que dia lindo hoje!",
            "Alguém tem alguma dica interessante?",
            "Vamos discutir sobre sistemas distribuídos!",
            "Que tal falarmos sobre ZeroMQ?",
            "Alguém já usou Docker em produção?",
            "Vamos compartilhar experiências!",
            "Que tal um café virtual?",
            "Alguém tem projetos interessantes para mostrar?"
        ]
    
    def generate_username(self):
        """Gera um nome de usuário aleatório para o bot"""
        adjectives = ["Rapido", "Inteligente", "Curioso", "Amigavel", "Criativo", "Ativo", "Sabio", "Divertido"]
        nouns = ["Bot", "Robo", "Assistente", "Helper", "Amigo", "Companheiro", "Ajudante", "Guia"]
        
        adjective = random.choice(adjectives)
        noun = random.choice(nouns)
        number = random.randint(1, 999)
        
        return f"{adjective}{noun}{number}"
    
    def start(self):
        """Inicia o bot"""
        try:
            # Criar contexto ZeroMQ
            self.context = zmq.Context()
            
            # Criar socket REQ para comunicação com servidor
            self.req_socket = self.context.socket(zmq.REQ)
            self.req_socket.connect("tcp://chat-server:5555")
            logger.info("Conectado ao servidor REQ-REP")
            
            # Criar socket SUB para receber mensagens
            self.sub_socket = self.context.socket(zmq.SUB)
            self.sub_socket.connect("tcp://pubsub-proxy:5558")
            logger.info("Conectado ao proxy Pub/Sub")
            
            # Gerar nome de usuário e fazer login
            self.username = self.generate_username()
            if not self.login():
                logger.error("Falha no login do bot")
                return False
            
            # Carregar canais disponíveis
            if not self.load_channels():
                logger.error("Falha ao carregar canais")
                return False
            
            # Subscrever para mensagens diretas
            self.subscribe_to_user()
            
            # Iniciar loop principal
            self.running = True
            logger.info(f"Bot '{self.username}' iniciado com sucesso!")
            self.run_loop()
            
        except Exception as e:
            logger.error(f"Erro ao iniciar bot: {e}")
            return False
    
    def login(self):
        """Realiza login no servidor"""
        try:
            request = {
                "service": "login",
                "data": {
                    "user": self.username,
                    "timestamp": datetime.now().isoformat()
                }
            }
            
            response = self.send_request(request)
            if response and response.get("data", {}).get("status") == "sucesso":
                logger.info(f"Login realizado com sucesso: {self.username}")
                return True
            else:
                logger.error(f"Falha no login: {response}")
                return False
                
        except Exception as e:
            logger.error(f"Erro no login: {e}")
            return False
    
    def load_channels(self):
        """Carrega lista de canais disponíveis"""
        try:
            request = {
                "service": "channels",
                "data": {
                    "timestamp": datetime.now().isoformat()
                }
            }
            
            response = self.send_request(request)
            if response and "users" in response.get("data", {}):
                self.available_channels = response["data"]["users"]
                logger.info(f"Canais disponíveis: {self.available_channels}")
                return True
            else:
                logger.warning("Nenhum canal disponível, criando canais padrão")
                return self.create_default_channels()
                
        except Exception as e:
            logger.error(f"Erro ao carregar canais: {e}")
            return False
    
    def create_default_channels(self):
        """Cria canais padrão se não houver nenhum"""
        default_channels = ["geral", "tech", "random", "bot-chat"]
        
        for channel in default_channels:
            try:
                request = {
                    "service": "channel",
                    "data": {
                        "channel": channel,
                        "timestamp": datetime.now().isoformat()
                    }
                }
                
                response = self.send_request(request)
                if response and response.get("data", {}).get("status") == "sucesso":
                    logger.info(f"Canal '{channel}' criado")
                    self.available_channels.append(channel)
                    
            except Exception as e:
                logger.error(f"Erro ao criar canal '{channel}': {e}")
        
        return len(self.available_channels) > 0
    
    def subscribe_to_user(self):
        """Subscreve para receber mensagens diretas"""
        try:
            topic = f"user_{self.username}"
            self.sub_socket.setsockopt_string(zmq.SUBSCRIBE, topic)
            logger.info(f"Inscrito para mensagens diretas: {topic}")
        except Exception as e:
            logger.error(f"Erro ao subscrever para mensagens diretas: {e}")
    
    def send_request(self, request):
        """Envia requisição para o servidor"""
        try:
            # Enviar requisição
            request_json = json.dumps(request)
            self.req_socket.send_string(request_json)
            
            # Receber resposta
            response_json = self.req_socket.recv_string()
            return json.loads(response_json)
            
        except Exception as e:
            logger.error(f"Erro ao enviar requisição: {e}")
            return None
    
    def publish_message(self, channel, message):
        """Publica mensagem em um canal"""
        try:
            request = {
                "service": "publish",
                "data": {
                    "user": self.username,
                    "channel": channel,
                    "message": message,
                    "timestamp": datetime.now().isoformat()
                }
            }
            
            response = self.send_request(request)
            if response and response.get("data", {}).get("status") == "OK":
                logger.info(f"Mensagem publicada no canal '{channel}': {message}")
                return True
            else:
                logger.error(f"Falha ao publicar mensagem: {response}")
                return False
                
        except Exception as e:
            logger.error(f"Erro ao publicar mensagem: {e}")
            return False
    
    def run_loop(self):
        """Loop principal do bot"""
        message_count = 0
        
        while self.running:
            try:
                # Escolher canal aleatório
                if not self.available_channels:
                    logger.warning("Nenhum canal disponível, aguardando...")
                    time.sleep(10)
                    continue
                
                channel = random.choice(self.available_channels)
                
                # Enviar 10 mensagens
                for i in range(10):
                    if not self.running:
                        break
                    
                    message = random.choice(self.messages)
                    if self.publish_message(channel, message):
                        message_count += 1
                        logger.info(f"Mensagem #{message_count} enviada")
                    
                    # Aguardar entre mensagens
                    time.sleep(random.uniform(2, 5))
                
                # Aguardar antes do próximo ciclo
                logger.info("Ciclo de mensagens concluído, aguardando próximo ciclo...")
                time.sleep(random.uniform(10, 20))
                
            except KeyboardInterrupt:
                logger.info("Interrupção recebida, encerrando bot...")
                break
            except Exception as e:
                logger.error(f"Erro no loop principal: {e}")
                time.sleep(5)
    
    def stop(self):
        """Para o bot e limpa recursos"""
        self.running = False
        
        if self.req_socket:
            self.req_socket.close()
            logger.info("Socket REQ fechado")
            
        if self.sub_socket:
            self.sub_socket.close()
            logger.info("Socket SUB fechado")
            
        if self.context:
            self.context.term()
            logger.info("Contexto ZeroMQ finalizado")
            
        logger.info("Bot encerrado")

def signal_handler(signum, frame):
    """Handler para sinais de interrupção"""
    logger.info(f"Recebido sinal {signum}, encerrando bot...")
    bot.stop()
    sys.exit(0)

if __name__ == "__main__":
    # Registrar handlers de sinal
    signal.signal(signal.SIGINT, signal_handler)
    signal.signal(signal.SIGTERM, signal_handler)
    
    # Criar e iniciar bot
    bot = ChatBot()
    
    try:
        bot.start()
    except KeyboardInterrupt:
        logger.info("Interrupção por teclado recebida")
    except Exception as e:
        logger.error(f"Erro inesperado: {e}")
    finally:
        bot.stop()
