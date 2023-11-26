package me.zedaster.mmvstesttask.repository.status;

import me.zedaster.mmvstesttask.model.FileStatus;

import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий, работающий со статусами обработки файлов
 */
public interface FileStatusRepository {
    /**
     * Добавляет файл в репозиторий со статусом уже происходящей над ним обработки какой-либо операции
     * @param filename Имя файла
     * @return Сгенерированный UUID для файла
     */
    UUID add(String filename);

    /**
     * Отмечает файл, как находящийся в обработке какой-либо операции
     * @param uuid UUID файла
     */
    void markProcessing(UUID uuid);

    /**
     * Отмечает завершение обработки последней операции над файлом
     * @param uuid UUID файла
     * @param isSuccess успешность последней операции
     */
    void setLastOperationStatus(UUID uuid, boolean isSuccess);

    /**
     * Получает статус файла
     * @param uuid UUID файла
     * @return Объект {@link Optional}. Пуст, если статус для такого UUID не найден.
     */
    Optional<FileStatus> getById(UUID uuid);

    /**
     * Проверяет, существует ли файл с таким UUID
     * @param uuid UUID файла
     * @return boolean
     */
    boolean has(UUID uuid);

    /**
     * Удаляет статус для файла
     * @param uuid UUID файла
     */
    void remove(UUID uuid);
}
