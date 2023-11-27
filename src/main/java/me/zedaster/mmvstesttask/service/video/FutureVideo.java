package me.zedaster.mmvstesttask.service.video;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Класс, содержащий UUID и асинхронную операцию загрузки будущего видеоролика
 */
public class FutureVideo {
    /**
     * UUID видеоролика
     */
    private final UUID uuid;

    /**
     * Асинхронная операция загрузки видеоролика
     */
    private final CompletableFuture<Boolean> uploadingOperation;

    public FutureVideo(UUID uuid, CompletableFuture<Boolean> completableFuture) {
        this.uuid = uuid;
        this.uploadingOperation = completableFuture;
    }

    /**
     * Возвращает UUID видеоролика
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * Возвращает асинхронную операцию загрузки видеоролика. Ответ содержит успешность операции
     */
    public CompletableFuture<Boolean> getUploadingOperation() {
        return uploadingOperation;
    }
}
