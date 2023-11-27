package me.zedaster.mmvstesttask.model;

import jakarta.persistence.*;

import java.util.Objects;
import java.util.UUID;

/**
 * Модель, содержащая информацию о файле и статусе его обработки
 */
@Entity
@Table(name = "FileStatuses")
public class FileStatus {
    /**
     * Сгенерированный UUID файла
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID uuid;

    /**
     * Имя файла
     */
    @Column
    private String fileName;

    /**
     * Статус последней операции над файлом
     */
    @Column
    private Boolean isLastProcessingSuccess;

    /**
     * Пустой конструктор для Hibernate
     */
    public FileStatus() {
        // empty
    }

    /**
     * Для работы по fileName в Hibernate
     */
    public FileStatus(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Новый статус файла
     *
     * @param uuid     UUID файла
     * @param fileName Имя файла
     */
    public FileStatus(UUID uuid, String fileName) {
        this.uuid = uuid;
        this.fileName = fileName;
        this.isLastProcessingSuccess = null;
    }

    /**
     * Отмечает начало процесса обработки файла
     */
    public void startProcessing() {
        this.isLastProcessingSuccess = null;
    }

    /**
     * Отмечает завершение процесса обработки файла
     * @param isSuccess успешно или нет
     */
    public void finishProcessing(boolean isSuccess) {
        this.isLastProcessingSuccess = isSuccess;
    }

    /**
     * Возвращает UUID файла
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * Возвращает имя файла
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Успешна ли последняя операция над файлом.
     * Вернет null, если никакие операции над файлом не завершились
     */
    public Boolean isLastProcessingSuccess() {
        return isLastProcessingSuccess;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileStatus fileStatus = (FileStatus) o;
        return Objects.equals(uuid, fileStatus.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}
