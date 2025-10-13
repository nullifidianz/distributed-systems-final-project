package models

import (
	"encoding/json"
	"time"
)

// RequestMessage representa uma mensagem de requisição para o servidor
type RequestMessage struct {
	Service string                 `json:"service"`
	Data    map[string]interface{} `json:"data"`
}

// ResponseMessage representa uma mensagem de resposta do servidor
type ResponseMessage struct {
	Service string                 `json:"service"`
	Data    map[string]interface{} `json:"data"`
}

// NewRequestMessage cria uma nova mensagem de requisição
func NewRequestMessage(service string) *RequestMessage {
	return &RequestMessage{
		Service: service,
		Data:    make(map[string]interface{}),
	}
}

// AddTimestamp adiciona timestamp atual à mensagem
func (rm *RequestMessage) AddTimestamp() {
	rm.Data["timestamp"] = time.Now()
}

// SetData adiciona dados à mensagem
func (rm *RequestMessage) SetData(key string, value interface{}) {
	rm.Data[key] = value
}

// ToJSON converte a mensagem para JSON
func (rm *RequestMessage) ToJSON() ([]byte, error) {
	return json.Marshal(rm)
}

// FromJSON converte JSON para mensagem
func (resp *ResponseMessage) FromJSON(data []byte) error {
	return json.Unmarshal(data, resp)
}

// GetStatus retorna o status da resposta
func (resp *ResponseMessage) GetStatus() string {
	if status, ok := resp.Data["status"].(string); ok {
		return status
	}
	return ""
}

// GetDescription retorna a descrição de erro se houver
func (resp *ResponseMessage) GetDescription() string {
	if description, ok := resp.Data["description"].(string); ok {
		return description
	}
	return ""
}

// GetMessage retorna a mensagem de erro se houver
func (resp *ResponseMessage) GetMessage() string {
	if message, ok := resp.Data["message"].(string); ok {
		return message
	}
	return ""
}

// GetUsers retorna lista de usuários
func (resp *ResponseMessage) GetUsers() []string {
	if users, ok := resp.Data["users"].([]interface{}); ok {
		result := make([]string, len(users))
		for i, user := range users {
			if name, ok := user.(string); ok {
				result[i] = name
			}
		}
		return result
	}
	return []string{}
}
