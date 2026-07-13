package com.example.spin.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// QR이 가리키는 가게가 내가 진행 중인 루트의 스탑이 아닌 경우
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class NotOnRouteException extends RuntimeException {

    public NotOnRouteException(String message) {
        super(message);
    }
}
