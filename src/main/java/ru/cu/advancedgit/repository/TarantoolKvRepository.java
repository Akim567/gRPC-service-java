package ru.cu.advancedgit.repository;

import io.tarantool.client.box.TarantoolBoxClient;
import ru.cu.advancedgit.model.KvEntry;

import java.util.Optional;

public class TarantoolKvRepository implements KvRepository {

    private static final String SPACE_NAME = "KV";

    private final TarantoolBoxClient client;

    public TarantoolKvRepository(TarantoolBoxClient client) {
        this.client = client;
    }

    @Override
    public void put(String key, byte[] value) {
        validateKey(key);
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Optional<KvEntry> get(String key) {
        validateKey(key);
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public boolean delete(String key) {
        validateKey(key);
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public long count() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    private void validateKey(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("key must not be null or blank");
        }
    }
}