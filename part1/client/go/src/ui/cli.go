package ui

import (
	"bufio"
	"fmt"
	"os"
	"strings"

	"distributed-systems-chat-client/src/client"

	"github.com/fatih/color"
)

// CLI representa a interface de linha de comando
type CLI struct {
	chatClient *client.ChatClient
	scanner    *bufio.Scanner
}

// NewCLI cria uma nova interface CLI
func NewCLI(chatClient *client.ChatClient) *CLI {
	return &CLI{
		chatClient: chatClient,
		scanner:    bufio.NewScanner(os.Stdin),
	}
}

// Run inicia a interface CLI
func (cli *CLI) Run() {
	cli.showWelcome()

	// Solicitar login
	if err := cli.handleLogin(); err != nil {
		fmt.Printf("%s Erro no login: %v\n", color.RedString("✗"), err)
		return
	}

	// Menu principal
	cli.showMainMenu()

	// Loop principal
	for {
		fmt.Print("\n> ")
		if !cli.scanner.Scan() {
			break
		}

		input := strings.TrimSpace(cli.scanner.Text())

		if input == "" {
			continue
		}

		if err := cli.handleCommand(input); err != nil {
			fmt.Printf("%s Erro: %v\n", color.RedString("✗"), err)
		}

		if input == "exit" || input == "quit" {
			break
		}
	}

	fmt.Println(color.CyanString("\nObrigado por usar o sistema de chat distribuído!"))
}

func (cli *CLI) showWelcome() {
	fmt.Println(color.CyanString("══════════════════════════════════════"))
	fmt.Println(color.CyanString("  Sistema de Chat Distribuído"))
	fmt.Println(color.CyanString("  Parte 1: Request-Reply"))
	fmt.Println(color.CyanString("══════════════════════════════════════"))
	fmt.Println()
}

func (cli *CLI) handleLogin() error {
	fmt.Print("Digite seu nome de usuário: ")
	if !cli.scanner.Scan() {
		return fmt.Errorf("entrada inválida")
	}

	username := strings.TrimSpace(cli.scanner.Text())
	if username == "" {
		return fmt.Errorf("nome de usuário não pode estar vazio")
	}

	return cli.chatClient.Login(username)
}

func (cli *CLI) showMainMenu() {
	fmt.Println(color.YellowString("\n══════════════════════════"))
	fmt.Println(color.YellowString("     MENU PRINCIPAL"))
	fmt.Println(color.YellowString("══════════════════════════"))
	fmt.Println("1. Listar usuários")
	fmt.Println("2. Listar canais")
	fmt.Println("3. Criar canal")
	fmt.Println("4. Exit")
	fmt.Println(color.YellowString("══════════════════════════"))
}

func (cli *CLI) handleCommand(input string) error {
	switch input {
	case "1", "usuarios", "users":
		return cli.listUsers()
	case "2", "canais", "channels":
		return cli.listChannels()
	case "3", "criar canal", "create":
		return cli.createChannel()
	case "4", "exit", "quit", "sair":
		return nil
	default:
		fmt.Println(color.YellowString("Comando não reconhecido. Use os comandos do menu:"))
		cli.showMainMenu()
		return nil
	}
}

func (cli *CLI) listUsers() error {
	users, err := cli.chatClient.ListUsers()
	if err != nil {
		return err
	}

	fmt.Println(color.GreenString("\n📋 USUÁRIOS CADASTRADOS:"))
	if len(users) == 0 {
		fmt.Println("  Nenhum usuário cadastrado.")
	} else {
		for i, user := range users {
			fmt.Printf("  %d. %s\n", i+1, user)
		}
	}

	return nil
}

func (cli *CLI) listChannels() error {
	channels, err := cli.chatClient.ListChannels()
	if err != nil {
		return err
	}

	fmt.Println(color.GreenString("\n📢 CANAIS DISPONÍVEIS:"))
	if len(channels) == 0 {
		fmt.Println("  Nenhum canal disponível.")
	} else {
		for i, channel := range channels {
			fmt.Printf("  %d. %s\n", i+1, channel)
		}
	}

	return nil
}

func (cli *CLI) createChannel() error {
	fmt.Print("Digite o nome do canal: ")
	if !cli.scanner.Scan() {
		return fmt.Errorf("entrada inválida")
	}

	channelName := strings.TrimSpace(cli.scanner.Text())
	if channelName == "" {
		return fmt.Errorf("nome do canal não pode estar vazio")
	}

	return cli.chatClient.CreateChannel(channelName)
}
