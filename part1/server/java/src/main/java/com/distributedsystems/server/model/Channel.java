package com.distributedsystems.server.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class Channel {
    @JsonProperty("channel")
    private String channel;
    
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
    
    public Channel() {}
    
    public Channel(String channel) {
        this.channel = channel;
        this.timestamp = LocalDateTime.now();
    }
    
    public Channel(String channel, LocalDateTime timestamp) {
        this.channel = channel;
        this.timestamp = timestamp;
    }
    
    public String getChannel() {
        return channel;
    }
    
    public void setChannel(String channel) {
        this.channel = channel;
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
        Channel channel = (Channel) obj;
        return this.channel != null ? this.channel.equals(channel.channel) : channel.channel == null;
    }
    
    @Override
    public int hashCode() {
        return channel != null ? channel.hashCode() : 0;
    }
}
