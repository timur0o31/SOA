package se.ifmo.ru.error.mappers;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ElementKind;
import jakarta.validation.Path;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.StringJoiner;
import java.util.stream.Collectors;

@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {
    @Override
    public Response toResponse(ConstraintViolationException exception) {
        String detail = exception.getConstraintViolations().stream()
                .map(violation ->
                        (violation.getPropertyPath()) + ": " + violation.getMessage())
                .distinct()
                .collect(Collectors.joining(";"));
        return ErrorResponses.build(Response.Status.BAD_REQUEST, detail);
    }
}
