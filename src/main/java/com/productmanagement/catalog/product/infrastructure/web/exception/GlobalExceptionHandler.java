package com.productmanagement.catalog.product.infrastructure.web.exception;

import com.productmanagement.catalog.product.domain.exception.CostUpperPriceException;
import com.productmanagement.catalog.product.domain.exception.InsufficientStockException;
import com.productmanagement.catalog.product.domain.exception.InvalidProductNameException;
import com.productmanagement.catalog.product.domain.exception.InvalidProductStateTransitionException;
import com.productmanagement.catalog.product.domain.exception.InvalidQuantityException;
import com.productmanagement.catalog.product.domain.exception.PriceBelowCostException;
import com.productmanagement.catalog.product.domain.exception.ProductAlreadyExistsException;
import com.productmanagement.catalog.product.domain.exception.ProductHasStockException;
import com.productmanagement.catalog.product.domain.exception.ProductNotFoundException;
import com.productmanagement.catalog.product.domain.exception.SameCostException;
import com.productmanagement.catalog.product.domain.exception.SamePriceException;
import com.productmanagement.catalog.product.infrastructure.web.response.ApiResult;
import com.productmanagement.catalog.product.infrastructure.web.response.ErrorCode;
import com.productmanagement.shared.exception.InvalidMonetaryValueException;
import jakarta.persistence.OptimisticLockException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidProductNameException.class)
    public ResponseEntity<ApiResult<Void>> handleInvalidProductNameException(InvalidProductNameException ex) {
        return buildError(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_PRODUCT_NAME, ex.getMessage());
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ApiResult<Void>> handleProductNotFoundException(ProductNotFoundException ex) {
        return buildError(HttpStatus.NOT_FOUND, ErrorCode.PRODUCT_NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ProductAlreadyExistsException.class)
    public ResponseEntity<ApiResult<Void>> handleProductAlreadyExistsException(ProductAlreadyExistsException ex) {
        return buildError(HttpStatus.CONFLICT, ErrorCode.PRODUCT_ALREADY_EXISTS, ex.getMessage());
    }

    @ExceptionHandler(InvalidMonetaryValueException.class)
    public ResponseEntity<ApiResult<Void>> handleInvalidMonetaryValueException(InvalidMonetaryValueException ex) {
        return buildError(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_VALUE, ex.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResult<Void>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        return buildError(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_REQUEST_BODY, "Invalid request body");
    }

    @ExceptionHandler(PriceBelowCostException.class)
    public ResponseEntity<ApiResult<Void>> handlePriceBelowCostException(PriceBelowCostException ex) {
        return buildError(HttpStatus.BAD_REQUEST, ErrorCode.PRICE_BELOW_COST, ex.getMessage());
    }

    @ExceptionHandler(CostUpperPriceException.class)
    public ResponseEntity<ApiResult<Void>> handleCostUpperPriceException(CostUpperPriceException ex) {
        return buildError(HttpStatus.BAD_REQUEST, ErrorCode.COST_UPPER_PRICE, ex.getMessage());
    }

    @ExceptionHandler(SamePriceException.class)
    public ResponseEntity<ApiResult<Void>> handleSamePriceException(SamePriceException ex) {
        return buildError(HttpStatus.BAD_REQUEST, ErrorCode.SAME_PRICE, ex.getMessage());
    }

    @ExceptionHandler(SameCostException.class)
    public ResponseEntity<ApiResult<Void>> handleSameCostException(SameCostException ex) {
        return buildError(HttpStatus.BAD_REQUEST, ErrorCode.SAME_COST, ex.getMessage());
    }

    @ExceptionHandler(InvalidQuantityException.class)
    public ResponseEntity<ApiResult<Void>> handleInvalidQuantityException(InvalidQuantityException ex) {
        return buildError(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_QUANTITY, ex.getMessage());
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ApiResult<Void>> handleInsufficientStockException(InsufficientStockException ex) {
        return buildError(HttpStatus.CONFLICT, ErrorCode.INSUFFICIENT_STOCK, ex.getMessage());
    }

    @ExceptionHandler(InvalidProductStateTransitionException.class)
    public ResponseEntity<ApiResult<Void>> handleInvalidProductStateTransitionException(InvalidProductStateTransitionException ex) {
        return buildError(HttpStatus.CONFLICT, ErrorCode.INVALID_PRODUCT_STATE_TRANSITION, ex.getMessage());
    }

    @ExceptionHandler(ProductHasStockException.class)
    public ResponseEntity<ApiResult<Void>> handleProductHasStockException(ProductHasStockException ex) {
        return buildError(HttpStatus.CONFLICT, ErrorCode.CANNOT_DEACTIVATE_WITH_STOCK, ex.getMessage());
    }

    // Others
    @ExceptionHandler({ObjectOptimisticLockingFailureException.class, OptimisticLockException.class})
    public ResponseEntity<ApiResult<Void>> handleOptimisticLock() {
        return buildError(HttpStatus.CONFLICT, ErrorCode.OPTIMISTIC_LOCK_FAILURE, "The resource was changed by another request. Please try again");
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResult<Void>> handleConstraintViolation(
            ConstraintViolationException ex) {

        String errorMessage = ex.getConstraintViolations().stream().map(ConstraintViolation::getMessage).collect(Collectors.joining(", "));

        if (errorMessage.isBlank()) {
            errorMessage = "Validation failed";
        }

        return buildError(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR, errorMessage);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResult<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream().map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        if (errorMessage.isBlank()) {
            errorMessage = "Validation failed";
        }

        return buildError(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR, errorMessage);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResult<Void>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        String errorMessage;

        if (ex.getValue() != null) {
            errorMessage = ex.getName() + ": '" + ex.getValue() + "' is invalid";
        } else {
            errorMessage = ex.getName() + ": invalid value";
        }

        if (ex.getRequiredType() != null && ex.getRequiredType().isEnum()) {
            String acceptedValues = Arrays.stream(ex.getRequiredType().getEnumConstants())
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));

            errorMessage += ". Accepted Values: " + acceptedValues;
        }

        return buildError(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR, errorMessage);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResult<Void>> handleGenericException(Exception ex) {
        log.error("Unexpected error", ex);
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    // Helper method to build error responses
    private ResponseEntity<ApiResult<Void>> buildError(HttpStatus httpStatus, ErrorCode code, String message) {
        return ResponseEntity.status(httpStatus).body(ApiResult.fail(code, message));
    }
}
