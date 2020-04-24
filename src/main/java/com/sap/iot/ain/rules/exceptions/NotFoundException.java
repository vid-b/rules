
package com.sap.iot.ain.rules.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;



@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Object not found")
public class NotFoundException extends RuntimeException{
    public NotFoundException(String message) {
        super(message);
    }
}
