package se.ifmo.ru.repository.criteria;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import se.ifmo.ru.dto.request.PersonSearchFilterDto;
import se.ifmo.ru.model.Person;
import se.ifmo.ru.model.Person_;
import se.ifmo.ru.model.Location_;

import java.util.ArrayList;
import java.util.List;


public final class PersonCriteriaSearch {
    private PersonCriteriaSearch() {}

    public static Predicate[] predicates(CriteriaBuilder cb, Root<Person> person,
                                         PersonSearchFilterDto filter) {
        List<Predicate> predicates = new ArrayList<>();
        if (filter == null) return new Predicate[0];
        addPersonFields(cb, person, filter, predicates);
        addLocation(cb, person, filter, predicates);
        return predicates.toArray(Predicate[]::new);
    }

    private static void addPersonFields(CriteriaBuilder cb, Root<Person> person,
                                         PersonSearchFilterDto filter, List<Predicate> predicates) {
        CritPredicates.add(predicates, filter.getId(),
                value -> cb.equal(person.get(Person_.id), value));
        CritPredicates.add(predicates, filter.getBirthday(),
                value -> cb.equal(person.get(Person_.birthday), value));
        CritPredicates.add(predicates, filter.getBirthdayFrom(),
                value -> cb.greaterThanOrEqualTo(person.get(Person_.birthday), value));
        CritPredicates.add(predicates, filter.getBirthdayTo(),
                value -> cb.lessThanOrEqualTo(person.get(Person_.birthday), value));
        CritPredicates.add(predicates, filter.getWeight(),
                value -> cb.equal(person.get(Person_.weight), value));
        CritPredicates.add(predicates, filter.getWeightFrom(),
                value -> cb.ge(person.get(Person_.weight), value));
        CritPredicates.add(predicates, filter.getWeightTo(),
                value -> cb.le(person.get(Person_.weight), value));
    }

    private static void addLocation(CriteriaBuilder cb, Root<Person> person,
                                         PersonSearchFilterDto filter, List<Predicate> predicates) {
        CritPredicates.add(predicates, filter.getLocationX(),
                value -> cb.equal(person.get(Person_.location).get(Location_.x), value));
        CritPredicates.add(predicates, filter.getLocationY(),
                value -> cb.equal(person.get(Person_.location).get(Location_.y), value));
        if (filter.getLocationNameContains() != null)
            predicates.add(cb.like(cb.lower(person.get(Person_.location).get(Location_.name)),
                    "%" + filter.getLocationNameContains().toLowerCase() + "%"));
    }
}
