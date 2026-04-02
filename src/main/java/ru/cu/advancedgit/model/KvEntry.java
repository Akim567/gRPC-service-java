package ru.cu.advancedgit.model;

import java.util.Arrays;
import java.util.Objects;

public class KvEntry {

    private final String key;
    private final byte[] value;

    public KvEntry(String key, byte[] value) {
        this.key = key;
        this.value = value;
    }

    public static KvEntry of(String key, byte[] value) {
        return new KvEntry(key, value);
    }

    public String getKey() {
        return key;
    }

    public byte[] getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "KvEntry{" +
                "key='" + key + '\'' +
                ", value=" + (value == null ? null : Arrays.toString(value)) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KvEntry that)) return false;
        return Objects.equals(key, that.key) &&
                Arrays.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(key);
        result = 31 * result + Arrays.hashCode(value);
        return result;
    }
}