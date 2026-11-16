package se.ifmo.ru.error.mappers;

import jakarta.transaction.TransactionalException;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import se.ifmo.ru.error.exceptions.ApplicationException;

@Provider
@Slf4j
public class ExceptionHandler implements ExceptionMapper<Exception> {
    @Override
    public Response toResponse(Exception exception) {

        if (exception instanceof ApplicationException app)
            return ErrorResponses.build(app.status(), app.getMessage());
        if (exception instanceof TransactionalException) {
            log.error("Ошибка транзакции", exception);
            return ErrorResponses.build(Response.Status.INTERNAL_SERVER_ERROR, "Не удалось завершить транзакцию");
        }
        if (exception instanceof WebApplicationException webException)
            return ErrorResponses.build(webException.getResponse().getStatusInfo(), webException.getMessage());
        log.error("Неожиданная ошибка", exception);
        return ErrorResponses.build(Response.Status.INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера");
    }

}
