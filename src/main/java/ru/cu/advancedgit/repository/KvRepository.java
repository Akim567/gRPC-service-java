package ru.cu.advancedgit.repository;

import ru.cu.advancedgit.model.KvEntry;

import java.util.Optional;

public interface KvRepository {

    void put(String key, byte[] value);

    Optional<KvEntry> get(String key);

    boolean delete(String key);

    long count();
}