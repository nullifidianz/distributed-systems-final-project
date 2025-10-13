package client

import (
	"bytes"
	"fmt"
	"io"
	"net/http"
	"time"

	"distributed-systems-chat-client/src/models"

	"github.com/fatih/color"
)

// ChatClient representa um cliente de chat
type ChatClient struct {
	ServerURL  string
	Username   string
	httpClient *http.Client
}

// NewChatClient cria um novo cliente de chat
func NewChatClient(serverURL string) *ChatClient {
	return &ChatClient{
		ServerURL: serverURL,
		httpClient: &http.Client{
			Timeout: 30 * time.Second,
		},
	}
}

// Login realiza login do usuário
func (c *ChatClient) Login(username string) error {
	c.Username = username

	request := models.NewRequestMessage("login")
	request.SetData("user", username)
	request.AddTimestamp()

	response, err := c.sendRequest(request)
	if err != nil {
		return fmt.Errorf("erro ao fazer login: %v", err)
	}

	if response.GetStatus() == "sucesso" {
		fmt.Printf("%s Login realizado com sucesso!\n",
			color.GreenString("✓"))
		return nil
	} else {
		return fmt.Errorf("falha no login: %s", response.GetDescription())
	}
}

// ListUsers lista todos os usuários cadastrados
func (c *ChatClient) ListUsers() ([]string, error) {
	request := models.NewRequestMessage("users")
	request.AddTimestamp()

	response, err := c.sendRequest(request)
	if err != nil {
		return nil, fmt.Errorf("erro ao listar usuários: %v", err)
	}

	users := response.GetUsers()
	return users, nil
}

// CreateChannel cria um novo canal
func (c *ChatClient) CreateChannel(channelName string) error {
	request := models.NewRequestMessage("channel")
	request.SetData("channel", channelName)
	request.AddTimestamp()

	response, err := c.sendRequest(request)
	if err != nil {
		return fmt.Errorf("erro ao criar canal: %v", err)
	}

	if response.GetStatus() == "sucesso" {
		fmt.Printf("%s Canal '%s' criado com sucesso!\n",
			color.GreenString("✓"), channelName)
		return nil
	} else {
		return fmt.Errorf("falha ao criar canal: %s", response.GetDescription())
	}
}

// ListChannels lista todos os canais disponíveis
func (c *ChatClient) ListChannels() ([]string, error) {
	request := models.NewRequestMessage("channels")
	request.AddTimestamp()

	response, err := c.sendRequest(request)
	if err != nil {
		return nil, fmt.Errorf("erro ao listar canais: %v", err)
	}

	channels := response.GetUsers() // Especificação usa "users" para canais também
	return channels, nil
}

// sendRequest envia uma requisição para o servidor
func (c *ChatClient) sendRequest(request *models.RequestMessage) (*models.ResponseMessage, error) {
	jsonData, err := request.ToJSON()
	if err != nil {
		return nil, fmt.Errorf("erro ao serializar requisição: %v", err)
	}

	url := fmt.Sprintf("%s/api/request", c.ServerURL)
	req, err := http.NewRequest("POST", url, bytes.NewBuffer(jsonData))
	if err != nil {
		return nil, fmt.Errorf("erro ao criar requisição: %v", err)
	}

	req.Header.Set("Content-Type", "application/json")

	resp, err := c.httpClient.Do(req)
	if err != nil {
		return nil, fmt.Errorf("erro ao enviar requisição: %v", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("erro no servidor: status %d", resp.StatusCode)
	}

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, fmt.Errorf("erro ao ler resposta: %v", err)
	}

	var response models.ResponseMessage
	err = response.FromJSON(body)
	if err != nil {
		return nil, fmt.Errorf("erro ao deserializar resposta: %v", err)
	}

	return &response, nil
}
