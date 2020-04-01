package ru.kpfu.grpctask.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class MathServer {

    public static void main(String[] args) throws InterruptedException, IOException {
        System.out.println("gRPC Server is started");

        Server server = ServerBuilder.forPort(5052)
                .addService(new MathServiceImpl())
                .build();

        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread (()->{
            System.out.println("Received Shutdown Request");
            server.shutdown();
            System.out.println("Successfully stopped the server");
        }));

        server.awaitTermination();
    }
}
