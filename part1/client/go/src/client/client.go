package client

import (
	"encoding/json"
	"fmt"

	"distributed-systems-chat-client/src/models"

	"github.com/fatih/color"
	"github.com/pebbe/zmq4"
)

// ChatClient representa um cliente de chat
type ChatClient struct {
	ServerURL string
	Username  string
	reqSocket *zmq4.Socket
	subSocket *zmq4.Socket
}

// NewChatClient cria um novo cliente de chat
func NewChatClient(serverURL string) *ChatClient {
	// Criar socket REQ para comunicação com servidor
	reqSocket, err := zmq4.NewSocket(zmq4.Type(zmq4.REQ))
	if err != nil {
		panic(fmt.Sprintf("Erro ao criar socket REQ: %v", err))
	}

	// Criar socket SUB para receber mensagens
	subSocket, err := zmq4.NewSocket(zmq4.Type(zmq4.SUB))
	if err != nil {
		panic(fmt.Sprintf("Erro ao criar socket SUB: %v", err))
	}

	// Conectar ao servidor REQ-REP
	err = reqSocket.Connect("tcp://chat-server:5555")
	if err != nil {
		panic(fmt.Sprintf("Erro ao conectar ao servidor: %v", err))
	}

	// Conectar ao proxy Pub/Sub
	err = subSocket.Connect("tcp://pubsub-proxy:5558")
	if err != nil {
		panic(fmt.Sprintf("Erro ao conectar ao proxy Pub/Sub: %v", err))
	}

	return &ChatClient{
		ServerURL: serverURL,
		reqSocket: reqSocket,
		subSocket: subSocket,
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

// sendRequest envia uma requisição para o servidor via ZeroMQ
func (c *ChatClient) sendRequest(request *models.RequestMessage) (*models.ResponseMessage, error) {
	jsonData, err := request.ToJSON()
	if err != nil {
		return nil, fmt.Errorf("erro ao serializar requisição: %v", err)
	}

	// Enviar requisição
	_, err = c.reqSocket.Send(string(jsonData), 0)
	if err != nil {
		return nil, fmt.Errorf("erro ao enviar requisição: %v", err)
	}

	// Receber resposta
	responseBytes, err := c.reqSocket.Recv(0)
	if err != nil {
		return nil, fmt.Errorf("erro ao receber resposta: %v", err)
	}

	var response models.ResponseMessage
	err = response.FromJSON([]byte(responseBytes))
	if err != nil {
		return nil, fmt.Errorf("erro ao deserializar resposta: %v", err)
	}

	return &response, nil
}

// PublishToChannel publica uma mensagem em um canal
func (c *ChatClient) PublishToChannel(channel, message string) error {
	request := models.NewRequestMessage("publish")
	request.SetData("user", c.Username)
	request.SetData("channel", channel)
	request.SetData("message", message)
	request.AddTimestamp()

	response, err := c.sendRequest(request)
	if err != nil {
		return fmt.Errorf("erro ao publicar mensagem: %v", err)
	}

	if response.GetStatus() == "OK" {
		fmt.Printf("%s Mensagem publicada no canal '%s'!\n",
			color.GreenString("✓"), channel)
		return nil
	} else {
		return fmt.Errorf("falha ao publicar: %s", response.GetMessage())
	}
}

// SendMessage envia uma mensagem direta para outro usuário
func (c *ChatClient) SendMessage(dst, message string) error {
	request := models.NewRequestMessage("message")
	request.SetData("src", c.Username)
	request.SetData("dst", dst)
	request.SetData("message", message)
	request.AddTimestamp()

	response, err := c.sendRequest(request)
	if err != nil {
		return fmt.Errorf("erro ao enviar mensagem: %v", err)
	}

	if response.GetStatus() == "OK" {
		fmt.Printf("%s Mensagem enviada para '%s'!\n",
			color.GreenString("✓"), dst)
		return nil
	} else {
		return fmt.Errorf("falha ao enviar mensagem: %s", response.GetMessage())
	}
}

// SubscribeToChannel subscreve a um canal para receber mensagens
func (c *ChatClient) SubscribeToChannel(channel string) error {
	topic := "canal_" + channel
	err := c.subSocket.SetSubscribe(topic)
	if err != nil {
		return fmt.Errorf("erro ao subscrever ao canal: %v", err)
	}

	fmt.Printf("%s Inscrito no canal '%s'\n",
		color.GreenString("✓"), channel)
	return nil
}

// SubscribeToUser subscreve para receber mensagens diretas
func (c *ChatClient) SubscribeToUser() error {
	topic := "user_" + c.Username
	err := c.subSocket.SetSubscribe(topic)
	if err != nil {
		return fmt.Errorf("erro ao subscrever para mensagens diretas: %v", err)
	}

	fmt.Printf("%s Inscrito para receber mensagens diretas\n",
		color.GreenString("✓"))
	return nil
}

// StartMessageListener inicia uma goroutine para escutar mensagens
func (c *ChatClient) StartMessageListener() {
	go func() {
		for {
			// Receber tópico
			topic, err := c.subSocket.Recv(0)
			if err != nil {
				fmt.Printf("%s Erro ao receber tópico: %v\n", color.RedString("✗"), err)
				continue
			}

			// Receber mensagem
			messageBytes, err := c.subSocket.Recv(0)
			if err != nil {
				fmt.Printf("%s Erro ao receber mensagem: %v\n", color.RedString("✗"), err)
				continue
			}

			// Parsear mensagem
			var messageData map[string]interface{}
			err = json.Unmarshal([]byte(messageBytes), &messageData)
			if err != nil {
				fmt.Printf("%s Erro ao parsear mensagem: %v\n", color.RedString("✗"), err)
				continue
			}

			// Exibir mensagem baseada no tipo de tópico
			if len(topic) > 6 && topic[:6] == "canal_" {
				channel := topic[6:]
				user := messageData["user"].(string)
				message := messageData["message"].(string)
				timestamp := messageData["timestamp"].(string)

				fmt.Printf("\n%s [%s] %s: %s\n",
					color.CyanString("📢 Canal "+channel),
					color.YellowString(timestamp),
					color.GreenString(user),
					message)
			} else if len(topic) > 5 && topic[:5] == "user_" {
				src := messageData["src"].(string)
				message := messageData["message"].(string)
				timestamp := messageData["timestamp"].(string)

				fmt.Printf("\n%s [%s] %s: %s\n",
					color.MagentaString("💬 Mensagem"),
					color.YellowString(timestamp),
					color.GreenString(src),
					message)
			}
		}
	}()
}

// Close fecha as conexões
func (c *ChatClient) Close() {
	if c.reqSocket != nil {
		c.reqSocket.Close()
	}
	if c.subSocket != nil {
		c.subSocket.Close()
	}
}
