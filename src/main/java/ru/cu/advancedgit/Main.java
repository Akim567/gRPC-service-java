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
            Object result = client.eval("return 'connected to tarantool'", Object.class).join().get();

            System.out.println("Connected successfully to Tarantool!");
            System.out.println("Response: " + result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}