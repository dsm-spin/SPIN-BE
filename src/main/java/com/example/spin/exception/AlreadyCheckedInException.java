package com.example.spin.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class AlreadyCheckedInException extends RuntimeException {

    public AlreadyCheckedInException(String message) {
        super(message);
    }
}
