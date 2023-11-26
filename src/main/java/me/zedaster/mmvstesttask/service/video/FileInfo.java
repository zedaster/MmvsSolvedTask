package me.zedaster.mmvstesttask.service.video;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import me.zedaster.mmvstesttask.model.FileStatus;

import java.util.UUID;

@JsonAutoDetect
public class FileInfo {
    @JsonIgnore
    private final FileStatus fileStatus;
    @JsonIgnore
    private final boolean isProcessing;

    public FileInfo(FileStatus status, boolean isProcessing) {
        this.fileStatus = status;
        this.isProcessing = isProcessing;
    }

    /**
     * Возвращает UUID файла
     */
    @JsonProperty("id")
    public UUID getUuid() {
        return fileStatus.getUuid();
    }

    /**
     * Возвращает имя файла
     */
    @JsonProperty("filename")
    public String getFileName() {
        return fileStatus.getFileName();
    }

    /**
     * Успешна ли последняя операция над файлом.
     * Вернет null, если никакие операции над файлом не завершились
     */
    @JsonProperty("processingSuccess")
    public Boolean isLastProcessingSuccess() {
        return fileStatus.isLastProcessingSuccess();
    }

    /**
     * Производиться ли операция над файлом сейчас
     *
     * @return
     */
    @JsonProperty("processing")
    public boolean isProcessing() {
        return isProcessing;
    }
}
