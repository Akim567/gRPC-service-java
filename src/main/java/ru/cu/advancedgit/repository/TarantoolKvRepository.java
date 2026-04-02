package ru.cu.advancedgit.repository;

import io.tarantool.client.box.TarantoolBoxClient;
import io.tarantool.mapping.TarantoolResponse;
import ru.cu.advancedgit.model.KvEntry;

import java.util.List;
import java.util.ArrayList;
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

        String lua = "return box.space['" + SPACE_NAME + "']:replace({...})";
        client.eval(lua, java.util.Arrays.asList(key, value)).join();
    }

    @Override
    public Optional<KvEntry> get(String key) {
        validateKey(key);

        String lua = "return box.space['" + SPACE_NAME + "']:get(...)";

        TarantoolResponse<?> response = client.eval(lua, List.of(key)).join();
        Object result = response.get();

        if (result == null) {
            return Optional.empty();
        }

        List<?> outer = (List<?>) result;
        if (outer.isEmpty()) {
            return Optional.empty();
        }

        Object tupleObj = outer.get(0);
        if (tupleObj == null) {
            return Optional.empty();
        }

        List<?> tuple = (List<?>) tupleObj;

        String storedKey = (String) tuple.get(0);
        Object valueObj = tuple.size() > 1 ? tuple.get(1) : null;

        byte[] value = null;
        if (valueObj != null) {
            if (valueObj instanceof byte[] bytes) {
                value = bytes;
            } else if (valueObj instanceof String str) {
                value = str.getBytes();
            } else {
                throw new IllegalStateException("Unexpected value type: " + valueObj.getClass());
            }
        }

        return Optional.of(KvEntry.of(storedKey, value));
    }

    @Override
    public boolean delete(String key) {
        validateKey(key);

        String lua = "local tuple = box.space['" + SPACE_NAME + "']:delete(...) " +
                "if tuple == nil then return false else return true end";

        TarantoolResponse<?> response = client.eval(lua, List.of(key)).join();
        Object result = response.get();

        if (result instanceof Boolean b) {
            return b;
        }

        if (result instanceof List<?> outer && !outer.isEmpty()) {
            Object first = outer.get(0);
            if (first instanceof Boolean b) {
                return b;
            }
        }

        throw new IllegalStateException("Unexpected delete result: " + result);
    }

    @Override
    public long count() {
        String lua = "return box.space['" + SPACE_NAME + "']:count()";

        TarantoolResponse<?> response = client.eval(lua).join();
        Object result = response.get();

        if (result == null) {
            return 0L;
        }

        if (result instanceof Number number) {
            return number.longValue();
        }

        if (result instanceof List<?> outer && !outer.isEmpty()) {
            Object first = outer.get(0);
            if (first instanceof Number number) {
                return number.longValue();
            }
        }

        throw new IllegalStateException("Unexpected count result: " + result);
    }

    public List<KvEntry> range(String keySince, String keyTo) {
        validateKey(keySince);
        validateKey(keyTo);

        if (keySince.compareTo(keyTo) > 0) {
            throw new IllegalArgumentException("keySince must be less than or equal to keyTo");
        }

        String lua =
                "local key_since, key_to = ...\n" +
                        "local result = {}\n" +
                        "for _, tuple in box.space['" + SPACE_NAME + "']:pairs(key_since, {iterator = 'GE'}) do\n" +
                        "    if tuple[1] > key_to then\n" +
                        "        break\n" +
                        "    end\n" +
                        "    table.insert(result, {tuple[1], tuple[2]})\n" +
                        "end\n" +
                        "return result";

        TarantoolResponse<?> response = client.eval(lua, List.of(keySince, keyTo)).join();
        Object result = response.get();

        List<KvEntry> entries = new java.util.ArrayList<>();

        if (result == null) {
            return entries;
        }

        if (!(result instanceof List<?> outer) || outer.isEmpty()) {
            return entries;
        }

        Object firstLevel = outer.get(0);
        if (!(firstLevel instanceof List<?> tuples)) {
            throw new IllegalStateException("Unexpected range result: " + result);
        }

        for (Object tupleObj : tuples) {
            if (!(tupleObj instanceof List<?> tuple) || tuple.isEmpty()) {
                continue;
            }

            String key = (String) tuple.get(0);
            Object valueObj = tuple.size() > 1 ? tuple.get(1) : null;

            byte[] value = null;
            if (valueObj != null) {
                if (valueObj instanceof byte[] bytes) {
                    value = bytes;
                } else if (valueObj instanceof String str) {
                    value = str.getBytes();
                } else {
                    throw new IllegalStateException(
                            "Unexpected value type in range(): " + valueObj.getClass()
                    );
                }
            }

            entries.add(KvEntry.of(key, value));
        }

        return entries;
    }

    private void validateKey(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("key must not be null or blank");
        }
    }
}