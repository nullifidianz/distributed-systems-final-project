package com.distributedsystems.server.server;

import com.distributedsystems.server.model.RequestMessage;
import com.distributedsystems.server.model.ResponseMessage;
import com.distributedsystems.server.service.DataService;
import com.distributedsystems.server.service.PublisherService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class ZmqServer {

    @Autowired
    private DataService dataService;

    @Autowired
    private PublisherService publisherService;

    private final ObjectMapper objectMapper;
    private final ExecutorService executor;
    private ZContext context;
    private ZMQ.Socket repSocket;
    private boolean running = false;

    public ZmqServer() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.executor = Executors.newSingleThreadExecutor();
    }

    public void start() {
        if (running) {
            return;
        }

        context = new ZContext();
        repSocket = context.createSocket(SocketType.REP);
        repSocket.bind("tcp://*:5555");

        running = true;

        executor.submit(() -> {
            System.out.println("ZeroMQ Server iniciado na porta 5555");

            while (running && !Thread.currentThread().isInterrupted()) {
                try {
                    // Receber requisição
                    byte[] requestBytes = repSocket.recv();
                    if (requestBytes == null) {
                        continue;
                    }

                    String requestJson = new String(requestBytes, ZMQ.CHARSET);
                    System.out.println("Requisição recebida: " + requestJson);

                    // Processar requisição
                    ResponseMessage response = processRequest(requestJson);

                    // Enviar resposta
                    String responseJson = objectMapper.writeValueAsString(response);
                    repSocket.send(responseJson.getBytes(ZMQ.CHARSET));

                } catch (Exception e) {
                    System.err.println("Erro ao processar requisição: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    public void stop() {
        running = false;
        if (repSocket != null) {
            repSocket.close();
        }
        if (context != null) {
            context.close();
        }
        executor.shutdown();
    }

    private ResponseMessage processRequest(String requestJson) {
        try {
            RequestMessage request = objectMapper.readValue(requestJson, RequestMessage.class);
            String service = request.getService();
            Map<String, Object> data = request.getData();
            LocalDateTime timestamp = LocalDateTime.now();

            return switch (service) {
                case "login" -> handleLogin(data, timestamp);
                case "users" -> handleListUsers(data, timestamp);
                case "channel" -> handleCreateChannel(data, timestamp);
                case "channels" -> handleListChannels(data, timestamp);
                case "publish" -> handlePublish(data, timestamp);
                case "message" -> handleMessage(data, timestamp);
                default -> createErrorResponse("Serviço não reconhecido", timestamp);
            };

        } catch (Exception e) {
            System.err.println("Erro ao processar requisição: " + e.getMessage());
            return createErrorResponse("Erro interno do servidor", LocalDateTime.now());
        }
    }

    private ResponseMessage handleLogin(Map<String, Object> data, LocalDateTime timestamp) {
        String username = (String) data.get("user");

        if (username == null || username.trim().isEmpty()) {
            return createLoginErrorResponse("Nome de usuário não pode ser vazio", timestamp);
        }

        com.distributedsystems.server.model.User user = new com.distributedsystems.server.model.User(username.trim(),
                LocalDateTime.now());
        boolean success = dataService.addUser(user);

        if (success) {
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("status", "sucesso");
            responseData.put("timestamp", timestamp);
            return new ResponseMessage("login", responseData);
        } else {
            return createLoginErrorResponse("Usuário já existe", timestamp);
        }
    }

    private ResponseMessage handleListUsers(Map<String, Object> data, LocalDateTime timestamp) {
        java.util.List<com.distributedsystems.server.model.User> users = dataService.getUsers();
        java.util.List<String> userNames = new java.util.ArrayList<>();

        for (com.distributedsystems.server.model.User user : users) {
            userNames.add(user.getUser());
        }

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("timestamp", timestamp);
        responseData.put("users", userNames);

        return new ResponseMessage("users", responseData);
    }

    private ResponseMessage handleCreateChannel(Map<String, Object> data, LocalDateTime timestamp) {
        String channelName = (String) data.get("channel");

        if (channelName == null || channelName.trim().isEmpty()) {
            return createChannelErrorResponse("Nome do canal não pode ser vazio", timestamp);
        }

        com.distributedsystems.server.model.Channel channel = new com.distributedsystems.server.model.Channel(
                channelName.trim(), LocalDateTime.now());
        boolean success = dataService.addChannel(channel);

        if (success) {
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("status", "sucesso");
            responseData.put("timestamp", timestamp);
            return new ResponseMessage("channel", responseData);
        } else {
            return createChannelErrorResponse("Canal já existe", timestamp);
        }
    }

    private ResponseMessage handleListChannels(Map<String, Object> data, LocalDateTime timestamp) {
        java.util.List<com.distributedsystems.server.model.Channel> channels = dataService.getChannels();
        java.util.List<String> channelNames = new java.util.ArrayList<>();

        for (com.distributedsystems.server.model.Channel channel : channels) {
            channelNames.add(channel.getChannel());
        }

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("timestamp", timestamp);
        responseData.put("users", channelNames); // Note: following the spec that uses "users" field for channels

        return new ResponseMessage("channels", responseData);
    }

    private ResponseMessage handlePublish(Map<String, Object> data, LocalDateTime timestamp) {
        String user = (String) data.get("user");
        String channel = (String) data.get("channel");
        String message = (String) data.get("message");

        if (user == null || user.trim().isEmpty()) {
            return createPublishErrorResponse("Usuário não pode ser vazio", timestamp);
        }
        if (channel == null || channel.trim().isEmpty()) {
            return createPublishErrorResponse("Canal não pode ser vazio", timestamp);
        }
        if (message == null || message.trim().isEmpty()) {
            return createPublishErrorResponse("Mensagem não pode ser vazia", timestamp);
        }

        // Verificar se o canal existe
        boolean channelExists = dataService.getChannels().stream()
                .anyMatch(c -> c.getChannel().equals(channel.trim()));

        if (!channelExists) {
            return createPublishErrorResponse("Canal não existe", timestamp);
        }

        // Publicar mensagem
        boolean success = publisherService.publishToChannel(user.trim(), channel.trim(), message.trim(), timestamp);

        if (success) {
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("status", "OK");
            responseData.put("timestamp", timestamp);
            return new ResponseMessage("publish", responseData);
        } else {
            return createPublishErrorResponse("Erro ao publicar mensagem", timestamp);
        }
    }

    private ResponseMessage handleMessage(Map<String, Object> data, LocalDateTime timestamp) {
        String src = (String) data.get("src");
        String dst = (String) data.get("dst");
        String message = (String) data.get("message");

        if (src == null || src.trim().isEmpty()) {
            return createMessageErrorResponse("Usuário origem não pode ser vazio", timestamp);
        }
        if (dst == null || dst.trim().isEmpty()) {
            return createMessageErrorResponse("Usuário destino não pode ser vazio", timestamp);
        }
        if (message == null || message.trim().isEmpty()) {
            return createMessageErrorResponse("Mensagem não pode ser vazia", timestamp);
        }

        // Verificar se o usuário destino existe
        boolean userExists = dataService.getUsers().stream()
                .anyMatch(u -> u.getUser().equals(dst.trim()));

        if (!userExists) {
            return createMessageErrorResponse("Usuário destino não existe", timestamp);
        }

        // Enviar mensagem
        boolean success = publisherService.sendMessage(src.trim(), dst.trim(), message.trim(), timestamp);

        if (success) {
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("status", "OK");
            responseData.put("timestamp", timestamp);
            return new ResponseMessage("message", responseData);
        } else {
            return createMessageErrorResponse("Erro ao enviar mensagem", timestamp);
        }
    }

    private ResponseMessage createLoginErrorResponse(String description, LocalDateTime timestamp) {
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("status", "erro");
        responseData.put("timestamp", timestamp);
        responseData.put("description", description);
        return new ResponseMessage("login", responseData);
    }

    private ResponseMessage createChannelErrorResponse(String description, LocalDateTime timestamp) {
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("status", "erro");
        responseData.put("timestamp", timestamp);
        responseData.put("description", description);
        return new ResponseMessage("channel", responseData);
    }

    private ResponseMessage createPublishErrorResponse(String description, LocalDateTime timestamp) {
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("status", "erro");
        responseData.put("message", description);
        responseData.put("timestamp", timestamp);
        return new ResponseMessage("publish", responseData);
    }

    private ResponseMessage createMessageErrorResponse(String description, LocalDateTime timestamp) {
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("status", "erro");
        responseData.put("message", description);
        responseData.put("timestamp", timestamp);
        return new ResponseMessage("message", responseData);
    }

    private ResponseMessage createErrorResponse(String description, LocalDateTime timestamp) {
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("status", "erro");
        responseData.put("description", description);
        responseData.put("timestamp", timestamp);
        return new ResponseMessage("error", responseData);
    }
}
