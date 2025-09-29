package com.distributedsystems.server.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public class ResponseMessage {
    @JsonProperty("service")
    private String service;
    
    @JsonProperty("data")
    private Map<String, Object> data;
    
    public ResponseMessage() {}
    
    public ResponseMessage(String service, Map<String, Object> data) {
        this.service = service;
        this.data = data;
    }
    
    public String getService() {
        return service;
    }
    
    public void setService(String service) {
        this.service = service;
    }
    
    public Map<String, Object> getData() {
        return data;
    }
    
    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}
