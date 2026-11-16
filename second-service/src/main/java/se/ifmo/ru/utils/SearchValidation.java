package se.ifmo.ru.utils;

import se.ifmo.ru.dto.request.SortDto;
import se.ifmo.ru.error.exceptions.RequestValidationException;
import java.util.List;
import java.util.Set;

public final class SearchValidation {

    public static void validateSorts(List<SortDto> sorts, Set<String> allowedFields) {
        if (sorts == null) return;
        for (SortDto sort : sorts) {
            if (sort == null || sort.field() == null || !allowedFields.contains(sort.field())) {
                throw new RequestValidationException("sort.field", "Неизвестное поле сортировки");
            }
            if (!"ASC".equals(sort.direction()) && !"DESC".equals(sort.direction())) {
                throw new RequestValidationException("sort.direction", "Ожидается ASC или DESC");
            }
        }
    }
}
