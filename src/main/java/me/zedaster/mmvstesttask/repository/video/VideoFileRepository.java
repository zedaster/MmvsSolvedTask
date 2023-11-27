package me.zedaster.mmvstesttask.repository.video;

import org.springframework.core.io.InputStreamSource;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface VideoFileRepository {
    /**
     * Асинхронно загружает видеофайл
     * @param uuid UUID видеофайла
     * @param inputStreamSource Интерфейс, через который можно открыть {@link java.io.InputStream}
     * @return Успешность операции загрузки
     */
    CompletableFuture<Boolean> runUploading(UUID uuid, InputStreamSource inputStreamSource);

    /**
     * Асинхронно изменяет размер загруженного видеофайла
     * @param uuid UUID видеофайла
     * @param width ширина
     * @param height высота
     * @throws NonExistentIdException если видеофайла с таким UUID не существует
     * @return Успешность операции изменения размера
     */
    CompletableFuture<Boolean> runAdjustingSize(UUID uuid, int width, int height) throws NonExistentIdException, ProcessingException;

    /**
     * Асинхронно удаляет видеофайл. Также прерывает операцию над файлом (загрузка, изменение размера и т.д.), если
     * она происходит.
     * @param uuid UUID видеофайла
     * @throws NonExistentIdException если видеофайла с таким UUID не существует
     */
    CompletableFuture<Void> runRemoving(UUID uuid) throws NonExistentIdException;

    /**
     * Проверяет, производиться ли операция над этим файлом
     * @param uuid UUID видеофайла
     * @return boolean
     */
    boolean isProcessing(UUID uuid);
}
