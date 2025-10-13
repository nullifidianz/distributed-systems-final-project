package com.distributedsystems.server;

import com.distributedsystems.server.server.ZmqServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ServerApplication implements CommandLineRunner {

    @Autowired
    private ZmqServer zmqServer;

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        // Iniciar o servidor ZeroMQ
        zmqServer.start();

        // Adicionar shutdown hook para limpar recursos
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Encerrando servidor...");
            zmqServer.stop();
        }));
    }
}
