package com.distributedsystems.server.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class User {
    @JsonProperty("user")
    private String user;
    
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
    
    public User() {}
    
    public User(String user) {
        this.user = user;
        this.timestamp = LocalDateTime.now();
    }
    
    public User(String user, LocalDateTime timestamp) {
        this.user = user;
        this.timestamp = timestamp;
    }
    
    public String getUser() {
        return user;
    }
    
    public void setUser(String user) {
        this.user = user;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return this.user != null ? this.user.equals(user.user) : user.user == null;
    }
    
    @Override
    public int hashCode() {
        return user != null ? user.hashCode() : 0;
    }
}
