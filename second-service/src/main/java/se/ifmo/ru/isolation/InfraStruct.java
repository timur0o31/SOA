package se.ifmo.ru.isolation;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import se.ifmo.ru.enums.IsolationType;

import java.sql.Statement;
@ApplicationScoped
public class InfraStruct {

    @PersistenceContext
    private EntityManager em;

    public void setTransaction(IsolationType isolationType) {
        em.unwrap(Session.class).doWork(connection -> {
            try (Statement statement = connection.createStatement()) {
                statement.execute("SET TRANSACTION ISOLATION LEVEL " + isolationType.getType());
            }
        });
    }

}
