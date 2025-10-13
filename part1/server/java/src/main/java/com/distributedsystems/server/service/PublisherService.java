package com.distributedsystems.server.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Service;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class PublisherService {

    private final String DATA_DIR = "data";
    private final String PUBLICATIONS_FILE = DATA_DIR + "/publications.json";
    private final String MESSAGES_FILE = DATA_DIR + "/messages.json";

    private final ObjectMapper objectMapper;
    private final ZContext context;
    private final ZMQ.Socket pubSocket;
    private final List<Publication> publications;
    private final List<Message> messages;

    public PublisherService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.publications = new ArrayList<>();
        this.messages = new ArrayList<>();

        // Criar o diretório data se não existir
        File dataDir = new File(DATA_DIR);
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }

        // Inicializar ZeroMQ
        this.context = new ZContext();
        this.pubSocket = context.createSocket(SocketType.PUB);
        this.pubSocket.connect("tcp://pubsub-proxy:5557"); // Conecta ao proxy XSUB

        // Carregar dados existentes
        loadPublications();
        loadMessages();

        System.out.println("PublisherService iniciado - conectado ao proxy Pub/Sub");
    }

    public boolean publishToChannel(String user, String channel, String message, LocalDateTime timestamp) {
        try {
            // Criar payload da publicação
            Map<String, Object> payload = new HashMap<>();
            payload.put("user", user);
            payload.put("message", message);
            payload.put("timestamp", timestamp);

            String payloadJson = objectMapper.writeValueAsString(payload);

            // Publicar no tópico do canal
            String topic = "canal_" + channel;
            pubSocket.sendMore(topic);
            pubSocket.send(payloadJson);

            // Salvar publicação
            Publication publication = new Publication(user, channel, message, timestamp);
            publications.add(publication);
            savePublications();

            System.out.println("Publicação enviada - Canal: " + channel + ", Usuário: " + user);
            return true;

        } catch (Exception e) {
            System.err.println("Erro ao publicar mensagem: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean sendMessage(String src, String dst, String message, LocalDateTime timestamp) {
        try {
            // Criar payload da mensagem
            Map<String, Object> payload = new HashMap<>();
            payload.put("src", src);
            payload.put("message", message);
            payload.put("timestamp", timestamp);

            String payloadJson = objectMapper.writeValueAsString(payload);

            // Publicar no tópico do usuário destino
            String topic = "user_" + dst;
            pubSocket.sendMore(topic);
            pubSocket.send(payloadJson);

            // Salvar mensagem
            Message msg = new Message(src, dst, message, timestamp);
            messages.add(msg);
            saveMessages();

            System.out.println("Mensagem enviada - De: " + src + ", Para: " + dst);
            return true;

        } catch (Exception e) {
            System.err.println("Erro ao enviar mensagem: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void loadPublications() {
        try {
            File file = new File(PUBLICATIONS_FILE);
            if (file.exists()) {
                Publication[] publicationArray = objectMapper.readValue(file, Publication[].class);
                publications.addAll(Arrays.asList(publicationArray));
            }
        } catch (IOException e) {
            System.err.println("Erro ao carregar publicações: " + e.getMessage());
        }
    }

    private void loadMessages() {
        try {
            File file = new File(MESSAGES_FILE);
            if (file.exists()) {
                Message[] messageArray = objectMapper.readValue(file, Message[].class);
                messages.addAll(Arrays.asList(messageArray));
            }
        } catch (IOException e) {
            System.err.println("Erro ao carregar mensagens: " + e.getMessage());
        }
    }

    private void savePublications() {
        try {
            File file = new File(PUBLICATIONS_FILE);
            objectMapper.writeValue(file, publications);
        } catch (IOException e) {
            System.err.println("Erro ao salvar publicações: " + e.getMessage());
        }
    }

    private void saveMessages() {
        try {
            File file = new File(MESSAGES_FILE);
            objectMapper.writeValue(file, messages);
        } catch (IOException e) {
            System.err.println("Erro ao salvar mensagens: " + e.getMessage());
        }
    }

    public void shutdown() {
        if (pubSocket != null) {
            pubSocket.close();
        }
        if (context != null) {
            context.close();
        }
    }

    // Classes internas para persistência
    public static class Publication {
        private String user;
        private String channel;
        private String message;
        private LocalDateTime timestamp;

        public Publication() {
        }

        public Publication(String user, String channel, String message, LocalDateTime timestamp) {
            this.user = user;
            this.channel = channel;
            this.message = message;
            this.timestamp = timestamp;
        }

        // Getters e Setters
        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public String getChannel() {
            return channel;
        }

        public void setChannel(String channel) {
            this.channel = channel;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }
    }

    public static class Message {
        private String src;
        private String dst;
        private String message;
        private LocalDateTime timestamp;

        public Message() {
        }

        public Message(String src, String dst, String message, LocalDateTime timestamp) {
            this.src = src;
            this.dst = dst;
            this.message = message;
            this.timestamp = timestamp;
        }

        // Getters e Setters
        public String getSrc() {
            return src;
        }

        public void setSrc(String src) {
            this.src = src;
        }

        public String getDst() {
            return dst;
        }

        public void setDst(String dst) {
            this.dst = dst;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }
    }
}
