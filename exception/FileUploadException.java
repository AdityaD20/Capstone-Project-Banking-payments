package com.aurionpro.app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// This annotation tells Spring to return a 500 status code if this exception is not caught elsewhere
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class FileUploadException extends RuntimeException {
    public FileUploadException(String message) {
        super(message);
    }

    public FileUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}