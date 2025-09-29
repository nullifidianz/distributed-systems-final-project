package com.distributedsystems.server.service;

import com.distributedsystems.server.model.Channel;
import com.distributedsystems.server.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
public class DataService {
    private final String DATA_DIR = "data";
    private final String USERS_FILE = DATA_DIR + "/users.json";
    private final String CHANNELS_FILE = DATA_DIR + "/channels.json";
    
    private final ObjectMapper objectMapper;
    private final Set<User> users;
    private final Set<Channel> channels;
    
    public DataService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.users = new HashSet<>();
        this.channels = new HashSet<>();
        
        // Cria o diretório data se não existir
        File dataDir = new File(DATA_DIR);
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
        
        loadUsers();
        loadChannels();
    }
    
    private void loadUsers() {
        try {
            File file = new File(USERS_FILE);
            if (file.exists()) {
                User[] userArray = objectMapper.readValue(file, User[].class);
                users.addAll(Arrays.asList(userArray));
            }
        } catch (IOException e) {
            System.err.println("Erro ao carregar usuários: " + e.getMessage());
        }
    }
    
    private void loadChannels() {
        try {
            File file = new File(CHANNELS_FILE);
            if (file.exists()) {
                Channel[] channelArray = objectMapper.readValue(file, Channel[].class);
                channels.addAll(Arrays.asList(channelArray));
            }
        } catch (IOException e) {
            System.err.println("Erro ao carregar canais: " + e.getMessage());
        }
    }
    
    public synchronized boolean addUser(User user) {
        if (users.contains(user)) {
            return false;
        }
        users.add(user);
        saveUsers();
        return true;
    }
    
    public synchronized List<User> getUsers() {
        return new ArrayList<>(users);
    }
    
    public synchronized boolean addChannel(Channel channel) {
        if (channels.contains(channel)) {
            return false;
        }
        channels.add(channel);
        saveChannels();
        return true;
    }
    
    public synchronized List<Channel> getChannels() {
        return new ArrayList<>(channels);
    }
    
    private void saveUsers() {
        try {
            File file = new File(USERS_FILE);
            objectMapper.writeValue(file, users);
        } catch (IOException e) {
            System.err.println("Erro ao salvar usuários: " + e.getMessage());
        }
    }
    
    private void saveChannels() {
        try {
            File file = new File(CHANNELS_FILE);
            objectMapper.writeValue(file, channels);
        } catch (IOException e) {
            System.err.println("Erro ao salvar canais: " + e.getMessage());
        }
    }
}
