package ru.cu.advancedgit;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.tarantool.client.box.TarantoolBoxClient;
import io.tarantool.client.factory.TarantoolBoxClientBuilder;
import io.tarantool.client.factory.TarantoolFactory;
import io.tarantool.pool.InstanceConnectionGroup;
import ru.cu.advancedgit.grpc.KvServiceImpl;
import ru.cu.advancedgit.repository.TarantoolKvRepository;

import java.util.Collections;

public class Main {

    public static void main(String[] args) throws Exception {
        InstanceConnectionGroup connectionGroup = InstanceConnectionGroup.builder()
                .withHost("localhost")
                .withPort(3301)
                .withUser("app")
                .withPassword("app")
                .build();

        TarantoolBoxClientBuilder clientBuilder =
                TarantoolFactory.box().withGroups(Collections.singletonList(connectionGroup));

        TarantoolBoxClient client = clientBuilder.build();
        TarantoolKvRepository repo = new TarantoolKvRepository(client);
        KvServiceImpl kvService = new KvServiceImpl(repo);

        Server server = ServerBuilder.forPort(9090)
                .addService(kvService)
                .build();

        server.start();
        System.out.println("gRPC server started on port 9090");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down gRPC server...");
            server.shutdown();
            try {
                client.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));

        server.awaitTermination();
    }
}