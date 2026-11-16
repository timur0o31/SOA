package se.ifmo.ru.validation;

import jakarta.validation.ConstraintValidatorContext;
public interface RangesValidator {
    default <T extends Comparable<T>> boolean isValidRange(T fromValue, T toValue) {
        if (fromValue == null || toValue == null) {
            return true;
        }
        return fromValue.compareTo(toValue) <= 0;
    }

    default <T extends Comparable<T>> boolean validateRange(T fromValue, T toValue, String field,
                                                            String message, ConstraintValidatorContext context) {
        if (isValidRange(fromValue, toValue))
            return true;
        addMessage(context, field, message);
        return false;
    }

    default void addMessage(ConstraintValidatorContext context,String field, String message) {
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(field)
                .addConstraintViolation();
    }
    default boolean validateValueOrRange(Object exact, Object from, Object to,
            String field, ConstraintValidatorContext context) {
        boolean exactSet = exact != null;
        boolean rangeSet = from != null || to != null;
        if (exactSet && rangeSet) {
            context.buildConstraintViolationWithTemplate("Точное значение нельзя передавать вместе с диапазоном")
                    .addPropertyNode(field)
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}
