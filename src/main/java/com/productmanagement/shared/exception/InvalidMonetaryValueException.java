package com.productmanagement.shared.exception;

public class InvalidMonetaryValueException extends RuntimeException {
    public InvalidMonetaryValueException() {
        super();
    }

    public InvalidMonetaryValueException(String message) {
        super(message);
    }

}
