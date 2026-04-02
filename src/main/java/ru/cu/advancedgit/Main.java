package ru.cu.advancedgit;

import io.tarantool.client.box.TarantoolBoxClient;
import io.tarantool.client.factory.TarantoolBoxClientBuilder;
import io.tarantool.client.factory.TarantoolFactory;
import io.tarantool.mapping.TarantoolResponse;
import io.tarantool.pool.InstanceConnectionGroup;

import java.util.Collections;
import java.util.List;

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
            TarantoolResponse<List<String>> response =
                    client.eval("return {'connected to tarantool'}", String.class).join();

            System.out.println("Connected successfully");
            System.out.println(response.get());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}