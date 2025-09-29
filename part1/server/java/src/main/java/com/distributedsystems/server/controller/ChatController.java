package com.distributedsystems.server.controller;

import com.distributedsystems.server.model.Channel;
import com.distributedsystems.server.model.RequestMessage;
import com.distributedsystems.server.model.ResponseMessage;
import com.distributedsystems.server.model.User;
import com.distributedsystems.server.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ChatController {
    
    @Autowired
    private DataService dataService;
    
    @PostMapping("/request")
    public ResponseEntity<ResponseMessage> handleRequest(@RequestBody RequestMessage request) {
        String service = request.getService();
        Map<String, Object> data = request.getData();
        LocalDateTime timestamp = LocalDateTime.now();
        
        ResponseMessage response = switch (service) {
            case "login" -> handleLogin(data, timestamp);
            case "users" -> handleListUsers(data, timestamp);
            case "channel" -> handleCreateChannel(data, timestamp);
            case "channels" -> handleListChannels(data, timestamp);
            default -> createErrorResponse("Serviço não reconhecido", timestamp);
        };
        
        return ResponseEntity.ok(response);
    }
    
    private ResponseMessage handleLogin(Map<String, Object> data, LocalDateTime timestamp) {
        String username = (String) data.get("user");
        
        if (username == null || username.trim().isEmpty()) {
            return createLoginErrorResponse("Nome de usuário não pode ser vazio", timestamp);
        }
        
        User user = new User(username.trim(), LocalDateTime.now());
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
        List<User> users = dataService.getUsers();
        List<String> userNames = new ArrayList<>();
        
        for (User user : users) {
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
        
        Channel channel = new Channel(channelName.trim(), LocalDateTime.now());
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
        List<Channel> channels = dataService.getChannels();
        List<String> channelNames = new ArrayList<>();
        
        for (Channel channel : channels) {
            channelNames.add(channel.getChannel());
        }
        
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("timestamp", timestamp);
        responseData.put("users", channelNames); // Note: following the spec that uses "users" field for channels
        
        return new ResponseMessage("channels", responseData);
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
    
    private ResponseMessage createErrorResponse(String description, LocalDateTime timestamp) {
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("status", "erro");
        responseData.put("timestamp", timestamp);
        responseData.put("description", description);
        return new ResponseMessage("error", responseData);
    }
}
