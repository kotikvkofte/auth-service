package org.ex9.authservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO для представления ошибок в ответах API.
 * @author Краковцев Артём
 */
@Data
@Schema(description = "Response containing error details")
public class ErrorResponse {

    /**
     * Сообщение об ошибке.
     */
    @Schema(description = "Error message describing the issue", example = "User with login user1 already exists")
    private String message;

    /**
     * Время возникновения ошибки.
     */
    @Schema(description = "Timestamp when the error occurred", example = "2025-07-21T18:08:00")
    private LocalDateTime timestamp;

    /**
     * Конструктор для создания ответа с ошибкой.
     *
     * @param message Сообщение об ошибке.
     */
    public ErrorResponse(String message) {
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

}
