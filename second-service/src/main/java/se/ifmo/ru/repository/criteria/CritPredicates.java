package se.ifmo.ru.repository.criteria;

import jakarta.persistence.criteria.Predicate;

import java.util.List;
import java.util.function.Function;

public final class CritPredicates {
    public static <T> void add(List<Predicate> predicates, T value, Function<T, Predicate> function){
        if (value != null) predicates.add(function.apply(value));
    }
}
