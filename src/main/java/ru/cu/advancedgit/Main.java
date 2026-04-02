package ru.cu.advancedgit;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;
import io.tarantool.client.box.TarantoolBoxClient;
import io.tarantool.client.factory.TarantoolBoxClientBuilder;
import io.tarantool.client.factory.TarantoolFactory;
import io.tarantool.pool.InstanceConnectionGroup;
import ru.cu.advancedgit.grpc.KvServiceImpl;
import ru.cu.advancedgit.repository.TarantoolKvRepository;

import java.util.Collections;

public class Main {

    public static void main(String[] args) throws Exception {
        if (args.length > 0 && "--benchmark".equals(args[0])) {
            benchmarkScalability();
            return;
        }

        startGrpcServer();
    }

    private static void startGrpcServer() throws Exception {
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
                .addService(ProtoReflectionService.newInstance())
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

    private static void benchmarkScalability() throws Exception {
        System.out.println("Starting scalability benchmark...");

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

        try {
            // Test 1: Insert 100k records
            System.out.println("\n=== Test 1: Inserting 100,000 records ===");
            long startInsert = System.currentTimeMillis();
            for (int i = 0; i < 100_000; i++) {
                String key = String.format("key_%010d", i);
                byte[] value = ("value_" + i).getBytes();
                repo.put(key, value);

                if ((i + 1) % 10_000 == 0) {
                    System.out.println("Inserted " + (i + 1) + " records");
                }
            }
            long insertTime = System.currentTimeMillis() - startInsert;
            System.out.printf("Insert time: %d ms (%.2f records/sec)%n",
                insertTime, 100_000.0 / (insertTime / 1000.0));

            // Test 2: Count performance
            System.out.println("\n=== Test 2: Count Performance ===");
            long startCount = System.currentTimeMillis();
            long count = repo.count();
            long countTime = System.currentTimeMillis() - startCount;
            System.out.printf("Count: %d records, Time: %d ms%n", count, countTime);

            // Test 3: Range query performance
            System.out.println("\n=== Test 3: Range Query Performance ===");
            long startRange = System.currentTimeMillis();
            var entries = repo.range("key_0000000000", "key_0000050000", 10_000);
            long rangeTime = System.currentTimeMillis() - startRange;
            System.out.printf("Range query: found %d records, Time: %d ms%n", entries.size(), rangeTime);

            // Test 4: Get performance
            System.out.println("\n=== Test 4: Get Performance ===");
            long startGet = System.currentTimeMillis();
            for (int i = 0; i < 1_000; i++) {
                String key = String.format("key_%010d", i);
                repo.get(key);
            }
            long getTime = System.currentTimeMillis() - startGet;
            System.out.printf("1,000 Get operations, Time: %d ms (%.2f ops/sec)%n",
                getTime, 1_000.0 / (getTime / 1000.0));

            // Test 5: Delete performance
            System.out.println("\n=== Test 5: Delete Performance ===");
            long startDelete = System.currentTimeMillis();
            for (int i = 0; i < 1_000; i++) {
                String key = String.format("key_%010d", i);
                repo.delete(key);
            }
            long deleteTime = System.currentTimeMillis() - startDelete;
            System.out.printf("1,000 Delete operations, Time: %d ms (%.2f ops/sec)%n",
                deleteTime, 1_000.0 / (deleteTime / 1000.0));

            // Final count
            long finalCount = repo.count();
            System.out.printf("\nFinal count: %d records (expected ~99,000)%n", finalCount);

            System.out.println("\n=== Benchmark completed successfully ===");
        } finally {
            client.close();
        }
    }
}