#!/usr/bin/env python3
"""
ZeroMQ Proxy para Publisher-Subscriber
Conecta XSUB (porta 5557) com XPUB (porta 5558)
"""

import zmq
import sys
import signal
import logging

# Configurar logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

class ZmqProxy:
    def __init__(self):
        self.context = None
        self.xsub_socket = None
        self.xpub_socket = None
        self.running = False
        
    def start(self):
        """Inicia o proxy ZeroMQ"""
        try:
            # Criar contexto ZeroMQ
            self.context = zmq.Context()
            
            # Criar socket XSUB (recebe publicações)
            self.xsub_socket = self.context.socket(zmq.XSUB)
            self.xsub_socket.bind("tcp://*:5557")
            logger.info("Socket XSUB iniciado na porta 5557")
            
            # Criar socket XPUB (distribui para subscribers)
            self.xpub_socket = self.context.socket(zmq.XPUB)
            self.xpub_socket.bind("tcp://*:5558")
            logger.info("Socket XPUB iniciado na porta 5558")
            
            # Configurar proxy
            self.running = True
            logger.info("Proxy ZeroMQ iniciado - XSUB:5557 <-> XPUB:5558")
            
            # Iniciar proxy
            zmq.proxy(self.xsub_socket, self.xpub_socket)
            
        except Exception as e:
            logger.error(f"Erro ao iniciar proxy: {e}")
            self.stop()
            sys.exit(1)
    
    def stop(self):
        """Para o proxy e limpa recursos"""
        self.running = False
        
        if self.xsub_socket:
            self.xsub_socket.close()
            logger.info("Socket XSUB fechado")
            
        if self.xpub_socket:
            self.xpub_socket.close()
            logger.info("Socket XPUB fechado")
            
        if self.context:
            self.context.term()
            logger.info("Contexto ZeroMQ finalizado")
            
        logger.info("Proxy ZeroMQ encerrado")

def signal_handler(signum, frame):
    """Handler para sinais de interrupção"""
    logger.info(f"Recebido sinal {signum}, encerrando proxy...")
    proxy.stop()
    sys.exit(0)

if __name__ == "__main__":
    # Registrar handlers de sinal
    signal.signal(signal.SIGINT, signal_handler)
    signal.signal(signal.SIGTERM, signal_handler)
    
    # Criar e iniciar proxy
    proxy = ZmqProxy()
    
    try:
        proxy.start()
    except KeyboardInterrupt:
        logger.info("Interrupção por teclado recebida")
    except Exception as e:
        logger.error(f"Erro inesperado: {e}")
    finally:
        proxy.stop()
