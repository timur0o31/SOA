package se.ifmo.ru.isolation;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.hibernate.StatelessSession;
import se.ifmo.ru.enums.IsolationType;
import se.ifmo.ru.repository.TicketRepository;

import java.sql.Statement;
@ApplicationScoped
public class InfraStruct {
    @Inject
    private TicketRepository ticketRepository;
    public void setTransaction(IsolationType isolationType) {
        StatelessSession session = ticketRepository.session();
        session.joinTransaction();
        session.doWork(connection -> {
            try (Statement statement = connection.createStatement()) {
                statement.execute("SET TRANSACTION ISOLATION LEVEL " + isolationType.getType());
            }
        });
    }

}
