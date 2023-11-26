package me.zedaster.mmvstesttask.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

/**
 * Ответ на запрос загрузки файла
 */
@ResponseStatus(value = HttpStatus.OK)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UploadResponse {
    /**
     * UUID файла загруженного файла.
     */
    @JsonProperty("id")
    private final UUID uuid;

    /**
     * Создает ответ на запрос загрузки файла.
     * @param uuid UUID загруженного файла.
     */
    public UploadResponse(UUID uuid) {
        this.uuid = uuid;
    }

    /**
     * Возвращает UUID загруженного файла.
     * @return UUID
     */
    public UUID getUuid() {
        return uuid;
    }
}
