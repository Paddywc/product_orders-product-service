package product.orders.productservice.api;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import product.orders.productservice.api.command.ProductCommandController;
import product.orders.productservice.api.query.ProductQueryController;
import product.orders.productservice.domain.exception.ConcurrentProductUpdateException;
import product.orders.productservice.domain.exception.ProductNotFoundException;

import java.util.stream.Collectors;

@ControllerAdvice(assignableTypes = {
        ProductQueryController.class,
        ProductCommandController.class
})
public class ProductControllerAdvice {


    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleProductNotFound(
            ProductNotFoundException ex
    ) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiErrorResponse.of(
                        HttpStatus.NOT_FOUND.value(),
                        "PRODUCT_NOT_FOUND",
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(ConcurrentProductUpdateException.class)
    public ResponseEntity<ApiErrorResponse> handleConcurrentUpdate(ConcurrentProductUpdateException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiErrorResponse.of(
                        HttpStatus.CONFLICT.value(),
                        "CONCURRENT_PRODUCT_UPDATE",
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationError(MethodArgumentNotValidException ex) {
        String details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of(
                        HttpStatus.BAD_REQUEST.value(),
                        "VALIDATION_ERROR",
                        details
                ));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of(
                        HttpStatus.BAD_REQUEST.value(),
                        "CONSTRAINT_VIOLATION",
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of(
                        HttpStatus.BAD_REQUEST.value(),
                        "INVALID_REQUEST_BODY",
                        "Request body is not readable or malformed: " + ex.getMessage()
                ));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of(
                        HttpStatus.BAD_REQUEST.value(),
                        "INVALID_REQUEST_PARAMETER",
                        "Invalid parameter type: " + ex.getMessage()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiErrorResponse.of(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "INTERNAL_ERROR",
                        "Unexpected error occurred: " + ex.getMessage()
                ));
    }

}
