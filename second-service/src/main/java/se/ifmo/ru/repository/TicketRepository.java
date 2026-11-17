package se.ifmo.ru.repository;

import jakarta.data.Sort;
import jakarta.data.page.PageRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import se.ifmo.ru.dto.request.TicketSearchFilterDto;
import se.ifmo.ru.error.exceptions.ResourceNotFoundException;
import se.ifmo.ru.model.Ticket;
import se.ifmo.ru.repository.criteria.TicketCriteriaSearch;

import java.util.ArrayList;
import java.util.List;
@ApplicationScoped
public class TicketRepository {

    @PersistenceContext(unitName = "default")
    private EntityManager em;

    @PersistenceContext(unitName = "repeatableRead")
    private EntityManager repeatableReadEm;

    @PersistenceContext(unitName = "Serializable")
    private EntityManager serializableEm;

    public List<Ticket> search(TicketSearchFilterDto filter, PageRequest pageRequest, List<Sort<Ticket>> sorts) {
        CriteriaBuilder cb = repeatableReadEm.getCriteriaBuilder();
        CriteriaQuery<Ticket> query = cb.createQuery(Ticket.class);
        Root<Ticket> root = query.from(Ticket.class);
        List<Order> orderBy = new ArrayList<>();
        if (sorts != null) {
            for (Sort<Ticket> sort : sorts) {
                Path<?> column = root;
                for (String part : sort.property().split("[.]")) {
                    column = column.get(part);
                }
                if (sort.isAscending()) {
                    orderBy.add(cb.asc(column));
                } else {
                    orderBy.add(cb.desc(column));
                }
            }
        }
        root.fetch("person", JoinType.LEFT);
        query.select(root)
                .where(TicketCriteriaSearch.predicates(cb, root, filter))
                .orderBy(orderBy);
        int offset = Math.toIntExact(Math.multiplyExact(pageRequest.page() - 1, (long) pageRequest.size()));
        return repeatableReadEm.createQuery(query)
                .setFirstResult(offset)
                .setMaxResults(pageRequest.size())
                .getResultList();
    }

    public Long count(TicketSearchFilterDto filter) {
        CriteriaBuilder cb = repeatableReadEm.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<Ticket> root = query.from(Ticket.class);
        query.select(cb.count(root))
                .where(TicketCriteriaSearch.predicates(cb, root, filter));
        return repeatableReadEm.createQuery(query).getSingleResult();
    }

    public Double getPriceSum(){
        return em.createQuery("SELECT sum(t.price) FROM Ticket t", Double.class)
                .getSingleResult();
    }

    public Double averagePrice(){
        return em.createQuery("SELECT avg(t.price) FROM Ticket t", Double.class)
                .getSingleResult();
    }

    public int deleteByEventId(Long eventId) {
        return em.createQuery("DELETE FROM Ticket t WHERE t.eventId = :eventId")
                .setParameter("eventId", eventId)
                .executeUpdate();
    }

    public int deleteByEventIdSerializable(Long eventId) {
        CriteriaBuilder cb = serializableEm.getCriteriaBuilder();
        CriteriaDelete<Ticket> query = cb.createCriteriaDelete(Ticket.class);
        Root<Ticket> ticket = query.from(Ticket.class);
        query.where(cb.equal(ticket.get("eventId"), eventId));
        return serializableEm.createQuery(query).executeUpdate();
    }

    public Long countByPerson(Long personId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<Ticket> ticket = query.from(Ticket.class);
        query.select(cb.count(ticket))
                .where(cb.equal(ticket.get("person").get("id"), personId));
        return em.createQuery(query).getSingleResult();
    }

    public Ticket findById(Long id){
        Ticket ticket = em.find(Ticket.class, id);
        if (ticket == null){
            throw new ResourceNotFoundException("Билет с id " + id + " не найден");
        }
        return ticket;
    }

    public void update(Ticket ticket){
        em.merge(ticket);
    }

    public void save(Ticket ticket){
        em.persist(ticket);
    }

    public void deleteById(Long id){
        Ticket ticket = em.find(Ticket.class, id);
        if (ticket != null){
            em.remove(ticket);
        } else {
            throw new ResourceNotFoundException("Билет с id " + id + " не найден");
        }
    }

    public void saveSerializable(Ticket ticket){
        serializableEm.persist(ticket);
    }
    public Ticket findByIdSerializable(Long id){
        Ticket ticket = serializableEm.find(Ticket.class, id);
        if (ticket == null){
            throw new ResourceNotFoundException("Билет с id " + id + " не найден");
        }
        return ticket;
    }
}
