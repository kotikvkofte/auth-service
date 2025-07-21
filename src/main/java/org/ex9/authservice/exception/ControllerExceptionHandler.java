package org.ex9.authservice.exception;

import org.springframework.http.HttpStatus;
import org.ex9.authservice.dto.ErrorResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Глобальный обработчик исключений для REST-контроллеров.
 * @author Краковцев Артём
 */
@RestControllerAdvice
public class ControllerExceptionHandler {

    /**
     * Обрабатывает исключение, когда пользователь уже существует.
     *
     * @param ex Исключение UserAlreadyExistsException.
     * @return Ответ с HTTP-статусом 400 и сообщением об ошибке.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ErrorResponse handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    /**
     * Обрабатывает исключение, когда роль не найдена.
     *
     * @param ex Исключение RoleNotFoundException.
     * @return Ответ с HTTP-статусом 400 и сообщением об ошибке.
     */
    @ExceptionHandler(RoleNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleRoleNotFoundException(RoleNotFoundException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    /**
     * Обрабатывает исключение, когда пользователь не найден.
     *
     * @param ex Исключение UsernameNotFoundException.
     * @return Ответ с HTTP-статусом 404 и сообщением об ошибке.
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleUsernameNotFoundException(UsernameNotFoundException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    /**
     * Обрабатывает исключение, когда пользователь не найден в БД.
     *
     * @param ex Исключение UserNotFoundException.
     * @return Ответ с HTTP-статусом 404 и сообщением об ошибке.
     */
    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleUserNotFoundException(UserNotFoundException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    /**
     * Обрабатывает исключение, когда доступ запрещён.
     *
     * @param ex Исключение AccessDeniedException.
     * @return Ответ с HTTP-статусом 403 и сообщением об ошибке.
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleAccessDeniedException(AccessDeniedException ex) {
        return new ErrorResponse("Access denied: " + ex.getMessage());
    }

    /**
     * Обрабатывает ошибки валидации входных данных.
     *
     * @param ex Исключение MethodArgumentNotValidException.
     * @return Ответ с HTTP-статусом 400 и описанием ошибок валидации.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorResponse handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return new ErrorResponse("Validation error: " + message);
    }

    /**
     * Обрабатывает непредвиденные ошибки сервера.
     *
     * @param ex Общее исключение.
     * @return Ответ с HTTP-статусом 500 и сообщением об ошибке.
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ErrorResponse handleGenericException(Exception ex) {
        ex.printStackTrace();
        return new ErrorResponse("Internal Server Error: " + ex.getMessage());
    }

}
