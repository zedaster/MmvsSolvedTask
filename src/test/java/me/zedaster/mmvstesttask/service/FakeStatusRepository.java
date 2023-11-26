package me.zedaster.mmvstesttask.service;

import me.zedaster.mmvstesttask.model.FileStatus;
import me.zedaster.mmvstesttask.repository.status.FileStatusRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Фейковое хранилище статусов на основе {@link HashMap}
 */
public class FakeStatusRepository implements FileStatusRepository {
    private final Map<UUID, FileStatus> statusMap = new HashMap<>();

    @Override
    public UUID add(String filename) {
        UUID uuid = UUID.randomUUID();
        statusMap.put(uuid, new FileStatus(uuid, filename));
        return uuid;
    }

    @Override
    public void markProcessing(UUID uuid) {
        statusMap.get(uuid).startProcessing();
    }

    @Override
    public void setLastOperationStatus(UUID uuid, boolean isSuccess) {
        statusMap.get(uuid).finishProcessing(isSuccess);
    }

    @Override
    public Optional<FileStatus> getById(UUID uuid) {
        return Optional.ofNullable(statusMap.get(uuid));
    }

    @Override
    public boolean has(UUID uuid) {
        return statusMap.containsKey(uuid);
    }

    @Override
    public void remove(UUID uuid) {
        statusMap.remove(uuid);
    }

    public void removeAll() {
        statusMap.clear();
    }
}
