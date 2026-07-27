package com.locuspark.api.exception;

import com.locuspark.api.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            InvalidCredentialsException.class,
            TokenMissingException.class,
            TokenInvalidException.class,
            UsernameNotFoundException.class
    })
    public ResponseEntity<ErrorResponse> handleUnauthorized(Exception ex, HttpServletRequest request) {
        return buildResponseEntity(HttpStatus.UNAUTHORIZED, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleConflict(UserAlreadyExistsException ex, HttpServletRequest request) {
        return buildResponseEntity(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(UserNotFoundException ex, HttpServletRequest request) {
        return buildResponseEntity(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return buildResponseEntity(HttpStatus.BAD_REQUEST, "Erro de validação: " + message, request.getRequestURI());
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<ErrorResponse> handleBadRequestParameter(Exception ex, HttpServletRequest request) {
        return buildResponseEntity(HttpStatus.BAD_REQUEST, "Parâmetro de requisição inválido: " + ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        return buildResponseEntity(HttpStatus.BAD_REQUEST, "Erro de integridade de dados. Verifique se os campos estão corretos.", request.getRequestURI());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        Throwable mostSpecificCause = ex.getMostSpecificCause();
        String detailMessage = mostSpecificCause.getMessage();
        String userFriendlyMessage = "Corpo da requisição inválido ou malformado. ";
        
        if (detailMessage != null) {
            if (detailMessage.contains("DiscountType")) {
                userFriendlyMessage += "O tipo de desconto fornecido é inválido. Valores permitidos: PERCENTAGE, FIXED_VALUE, FREE_HOURS.";
            } else {
                userFriendlyMessage += mostSpecificCause.getLocalizedMessage();
            }
        }
        return buildResponseEntity(HttpStatus.BAD_REQUEST, userFriendlyMessage, request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)

    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        return buildResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, "Ocorreu um erro inesperado no servidor.", request.getRequestURI());
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        // Regras de negócio violadas geralmente retornam HTTP 400 (Bad Request) ou 422 (Unprocessable Entity)
        return buildResponseEntity(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        // Recursos não encontrados mapeiam diretamente para o HTTP 404 (Not Found)
        return buildResponseEntity(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
    }

    private ResponseEntity<ErrorResponse> buildResponseEntity(HttpStatus status, String message, String path) {
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(ZoneOffset.UTC),
                status.value(),
                message,
                path
        );
        return ResponseEntity.status(status).body(error);
    }
}