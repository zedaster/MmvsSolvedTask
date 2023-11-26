package me.zedaster.mmvstesttask.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Ответ для операций, где требуется показать только успешность выполнения
 */
public class SuccessResponse {
    /**
     * Всегда возвращает true. В иных случаях использовать ${@link BadRequestResponse}
     */
    @JsonProperty("success")
    public boolean getSuccess() {
        return true;
    }
}
