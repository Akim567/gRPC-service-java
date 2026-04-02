package ru.cu.advancedgit;

import io.tarantool.client.box.TarantoolBoxClient;
import io.tarantool.client.factory.TarantoolBoxClientBuilder;
import io.tarantool.client.factory.TarantoolFactory;
import io.tarantool.pool.InstanceConnectionGroup;
import ru.cu.advancedgit.model.KvEntry;
import ru.cu.advancedgit.repository.TarantoolKvRepository;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Main {

    public static void main(String[] args) {
        InstanceConnectionGroup connectionGroup = InstanceConnectionGroup.builder()
                .withHost("localhost")
                .withPort(3301)
                .withUser("app")
                .withPassword("app")
                .build();

        TarantoolBoxClientBuilder clientBuilder =
                TarantoolFactory.box().withGroups(Collections.singletonList(connectionGroup));

        try (TarantoolBoxClient client = clientBuilder.build()) {
            TarantoolKvRepository repo = new TarantoolKvRepository(client);

            System.out.println("=== TEST START ===");

            repo.put("k1", "hello".getBytes(StandardCharsets.UTF_8));
            repo.put("k2", null);

            System.out.println("Put done");

            Optional<KvEntry> k1 = repo.get("k1");
            Optional<KvEntry> k2 = repo.get("k2");
            Optional<KvEntry> k3 = repo.get("k3");

            System.out.println("k1 = " + k1);
            System.out.println("k2 = " + k2);
            System.out.println("k3 = " + k3);

            long countBeforeDelete = repo.count();
            System.out.println("count = " + countBeforeDelete);

            boolean deleted1 = repo.delete("k1");
            boolean deleted2 = repo.delete("k1");

            System.out.println("delete k1 (first) = " + deleted1);
            System.out.println("delete k1 (second) = " + deleted2);

            long countAfterDelete = repo.count();
            System.out.println("count after delete = " + countAfterDelete);

            // Данные для range
            repo.put("a", "one".getBytes(StandardCharsets.UTF_8));
            repo.put("b", "two".getBytes(StandardCharsets.UTF_8));
            repo.put("c", null);
            repo.put("d", "four".getBytes(StandardCharsets.UTF_8));
            repo.put("e", "five".getBytes(StandardCharsets.UTF_8));

            List<KvEntry> rangeResult = repo.range("b", "d");

            System.out.println("range b..d:");
            for (KvEntry entry : rangeResult) {
                String valueAsString = entry.getValue() == null
                        ? "null"
                        : new String(entry.getValue(), StandardCharsets.UTF_8);

                System.out.println("  " + entry.getKey() + " -> " + valueAsString);
            }

            System.out.println("=== TEST END ===");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}