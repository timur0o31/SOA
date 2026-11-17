package se.ifmo.ru.repository;

import jakarta.data.Sort;
import jakarta.data.page.PageRequest;
import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Repository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.hibernate.Session;
import org.hibernate.StatelessSession;
import se.ifmo.ru.error.exceptions.ResourceNotFoundException;
import se.ifmo.ru.model.Person;
import se.ifmo.ru.dto.request.PersonSearchFilterDto;
import se.ifmo.ru.repository.criteria.PersonCriteriaSearch;

import java.util.ArrayList;
import java.util.List;
@ApplicationScoped
public class PersonRepository {

    @PersistenceContext(unitName="default")
    private EntityManager em;

    @PersistenceContext(unitName = "serializable")
    private EntityManager serializableEm;

    @PersistenceContext(unitName = "repeatableRead")
    private EntityManager repeatableEm;

    public List<Person> search(PersonSearchFilterDto filter, PageRequest pageRequest, List<Sort<Person>> sorts) {
        CriteriaBuilder cb = repeatableEm.getCriteriaBuilder();
        CriteriaQuery<Person> query = cb.createQuery(Person.class);
        Root<Person> root = query.from(Person.class);
        List<Order> orderBy = new ArrayList<>();
        if (sorts != null) {
            for (Sort<Person> sort : sorts) {
                Path<?> column = root;
                for (String part : sort.property().split("[.]")) {
                    column = column.get(part);
                }
                if (sort.isAscending())
                    orderBy.add(cb.asc(column));
                else
                    orderBy.add(cb.desc(column));
            }
        }
        query.select(root)
                .where(PersonCriteriaSearch.predicates(cb, root, filter))
                .orderBy(orderBy);

        int offset = Math.toIntExact(Math.multiplyExact(pageRequest.page() - 1, (long) pageRequest.size()));
        return repeatableEm.createQuery(query)
                .setFirstResult(offset)
                .setMaxResults(pageRequest.size())
                .getResultList();
    }

    public Long count(PersonSearchFilterDto filter) {
        CriteriaBuilder cb = repeatableEm.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<Person> root = query.from(Person.class);
        query.select(cb.count(root))
                .where(PersonCriteriaSearch.predicates(cb, root, filter));
        return repeatableEm.createQuery(query).getSingleResult();
    }

    public Person findById(Long id) {
        Person person = em.find(Person.class, id);
        if (person == null){
            throw new ResourceNotFoundException("Человек с id " + id + " не найден");
        }
        return person;
    }

    public void update(Person person){
        em.merge(person);
        em.flush();
    }

    public void save(Person person){
        em.persist(person);
    }

    public void deleteById(Long id){
        Person person = em.find(Person.class, id);
        if (person != null){
            em.remove(person);
            em.flush();
        }
    }

    public Person findByIdSerializable(Long id){
        return serializableEm.find(Person.class, id);
    }
    public void deleteByIdSerializable(Long id){
        Person person = serializableEm.find(Person.class, id);
        if (person != null){
            serializableEm.remove(person);
        }
    }
}
