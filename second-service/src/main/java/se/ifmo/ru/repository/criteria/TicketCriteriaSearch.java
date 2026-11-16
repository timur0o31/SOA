package se.ifmo.ru.repository.criteria;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import se.ifmo.ru.dto.request.TicketSearchFilterDto;
import se.ifmo.ru.model.Ticket;
import se.ifmo.ru.model.Ticket_;
import se.ifmo.ru.model.Person_;
import se.ifmo.ru.model.Coordinates_;
import se.ifmo.ru.model.Location_;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public final class TicketCriteriaSearch {
    private TicketCriteriaSearch() {}

    public static Predicate[] predicates(CriteriaBuilder cb, Root<Ticket> ticket,
                                         TicketSearchFilterDto filter) {
        List<Predicate> predicates = new ArrayList<>();
        if (filter == null) return new Predicate[0];
        addTicketFields(cb, ticket, filter, predicates);
        addCoordinates(cb, ticket, filter, predicates);
        addCreationDate(cb, ticket, filter, predicates);
        addPersonFields(cb, ticket, filter, predicates);
        return predicates.toArray(Predicate[]::new);
    }

    private static void addTicketFields(CriteriaBuilder cb, Root<Ticket> ticket,
                                         TicketSearchFilterDto filter, List<Predicate> predicates) {
        CritPredicates.add(predicates, filter.getId(),
                value -> cb.equal(ticket.get(Ticket_.id), value));

        if (filter.getNameContains() != null)
            predicates.add(cb.like(cb.lower(ticket.get(Ticket_.name)),
                    "%" + filter.getNameContains().toLowerCase() + "%"));
        CritPredicates.add(predicates, filter.getPriceFrom(),
                value -> cb.ge(ticket.get(Ticket_.price), value));
        CritPredicates.add(predicates, filter.getPriceTo(),
                value -> cb.le(ticket.get(Ticket_.price), value));

        if (filter.isTypeSpecified()) {
            if (filter.getType() == null) predicates.add(cb.isNull(ticket.get(Ticket_.type)));
            else predicates.add(cb.equal(ticket.get(Ticket_.type), filter.getType()));
        }
        CritPredicates.add(predicates, filter.getEventId(),
                value -> cb.equal(ticket.get(Ticket_.eventId), value));
    }

    private static void addCoordinates(CriteriaBuilder cb, Root<Ticket> ticket,
                                         TicketSearchFilterDto filter, List<Predicate> predicates) {
        CritPredicates.add(predicates, filter.getCoordinatesXFrom(),
                value -> cb.ge(ticket.get(Ticket_.coordinates).get(Coordinates_.x), value));
        CritPredicates.add(predicates, filter.getCoordinatesXTo(),
                value -> cb.le(ticket.get(Ticket_.coordinates).get(Coordinates_.x), value));
        CritPredicates.add(predicates, filter.getCoordinatesYFrom(),
                value -> cb.ge(ticket.get(Ticket_.coordinates).get(Coordinates_.y), value));
        CritPredicates.add(predicates, filter.getCoordinatesYTo(),
                value -> cb.le(ticket.get(Ticket_.coordinates).get(Coordinates_.y), value));
    }

    private static void addCreationDate(CriteriaBuilder cb, Root<Ticket> ticket,
                                         TicketSearchFilterDto filter, List<Predicate> predicates) {
        CritPredicates.add(predicates, filter.getCreationDateFrom(),
                value -> cb.greaterThanOrEqualTo(ticket.get(Ticket_.creationDate), Date.from(value.toInstant())));
        CritPredicates.add(predicates, filter.getCreationDateTo(),
                value -> cb.lessThanOrEqualTo(ticket.get(Ticket_.creationDate), Date.from(value.toInstant())));
    }

    private static void addPersonFields(CriteriaBuilder cb, Root<Ticket> ticket,
                                         TicketSearchFilterDto filter, List<Predicate> predicates) {
        CritPredicates.add(predicates, filter.getPersonBirthday(),
                value -> cb.equal(ticket.get(Ticket_.person).get(Person_.birthday), value));
        CritPredicates.add(predicates, filter.getPersonBirthdayFrom(),
                value -> cb.greaterThanOrEqualTo(ticket.get(Ticket_.person).get(Person_.birthday), value));
        CritPredicates.add(predicates, filter.getPersonBirthdayTo(),
                value -> cb.lessThanOrEqualTo(ticket.get(Ticket_.person).get(Person_.birthday), value));
        CritPredicates.add(predicates, filter.getPersonWeight(),
                value -> cb.equal(ticket.get(Ticket_.person).get(Person_.weight), value));
        CritPredicates.add(predicates, filter.getPersonWeightFrom(),
                value -> cb.ge(ticket.get(Ticket_.person).get(Person_.weight), value));
        CritPredicates.add(predicates, filter.getPersonWeightTo(),
                value -> cb.le(ticket.get(Ticket_.person).get(Person_.weight), value));
        CritPredicates.add(predicates, filter.getPersonLocationX(),
                value -> cb.equal(ticket.get(Ticket_.person).get(Person_.location).get(Location_.x), value));
        CritPredicates.add(predicates, filter.getPersonLocationY(),
                value -> cb.equal(ticket.get(Ticket_.person).get(Person_.location).get(Location_.y), value));
        if (filter.getPersonLocationNameContains() != null)
            predicates.add(cb.like(cb.lower(ticket.get(Ticket_.person).get(Person_.location).get(Location_.name)),
                    "%" + filter.getPersonLocationNameContains().toLowerCase() + "%"));
    }
}
