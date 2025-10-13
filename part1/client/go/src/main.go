package main

import (
	"fmt"
	"os"

	"distributed-systems-chat-client/src/client"
	"distributed-systems-chat-client/src/ui"

	"github.com/fatih/color"
	"github.com/joho/godotenv"
)

func main() {
	// Carregar variáveis de ambiente do arquivo .env se existir
	_ = godotenv.Load()

	// Obter URL do servidor das variáveis de ambiente ou usar padrão
	serverURL := os.Getenv("SERVER_URL")
	if serverURL == "" {
		serverURL = "http://localhost:8080"
	}

	fmt.Printf("Conectando ao servidor: %s\n", color.CyanString(serverURL))

	// Criar cliente de chat
	chatClient := client.NewChatClient(serverURL)

	// Criar e executar interface CLI
	cli := ui.NewCLI(chatClient)

	// Tratar erro de execução
	defer func() {
		if r := recover(); r != nil {
			fmt.Printf("%s Erro fatal: %v\n", color.RedString("✗"), r)
			os.Exit(1)
		}
	}()

	// Garantir que as conexões sejam fechadas
	defer chatClient.Close()

	cli.Run()
}
