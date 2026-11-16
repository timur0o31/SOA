package se.ifmo.ru.error.exceptions;

import jakarta.ws.rs.core.Response;

public class PersonConflictException extends ApplicationException {
    public PersonConflictException(String message) {
        super(Response.Status.CONFLICT, message, null);
    }
}
