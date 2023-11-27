package me.zedaster.mmvstesttask.controller.exception;

import me.zedaster.mmvstesttask.controller.response.BadRequestResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestResponse toResponse() {
        return new BadRequestResponse(this.getMessage());
    }
}
