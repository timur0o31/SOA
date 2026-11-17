package se.ifmo.ru.service;

import lombok.extern.slf4j.Slf4j;
import se.ifmo.ru.utils.SearchValidation;
import java.util.Set;
import jakarta.data.Sort;
import jakarta.data.page.PageRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import org.hibernate.StaleObjectStateException;
import org.hibernate.exception.ConstraintViolationException;
import org.postgresql.util.PSQLState;
import se.ifmo.ru.dto.request.PersonSearchFilterDto;
import se.ifmo.ru.error.exceptions.PersonConflictException;
import se.ifmo.ru.error.exceptions.ResourceNotFoundException;
import se.ifmo.ru.dto.request.PersonCreateDto;
import se.ifmo.ru.dto.request.SearchDto;
import se.ifmo.ru.dto.response.PersonResponseDto;
import se.ifmo.ru.dto.response.ResponsePaginationDto;
import se.ifmo.ru.mapper.PersonMapper;
import se.ifmo.ru.model.Person;
import se.ifmo.ru.repository.PersonRepository;

import java.util.*;

@Slf4j
@ApplicationScoped
public class PersonService {
    @Inject
    private PersonRepository personRepository;
    @Inject
    private PersonMapper personMapper;
    @Inject
    private TicketStatisticsService ticketStatisticsService;

    @Transactional(Transactional.TxType.REQUIRED)
    public PersonResponseDto findById(Long personId) {
        log.debug("Поиск человека: personId={}", personId);
        Person person = personRepository.findById(personId);
        return personMapper.toPersonResponseDto(person);
    }
    @Transactional(Transactional.TxType.REQUIRED)
    public PersonResponseDto savePerson(PersonCreateDto personCreateDto) {
        Person person = savePersonEntity(personCreateDto);
        return personMapper.toPersonResponseDto(person);
    }

    @Transactional(Transactional.TxType.REQUIRED)
    public Person savePersonEntity(PersonCreateDto personCreateDto) {
        log.info("Создание человека");
        Person person = personMapper.toPerson(personCreateDto);
        personRepository.save(person);
        log.info("Запрос на создание человека выполнен.");
        return person;
    }
    //суть в том что пока мы проверяем countByPerson, другой поток может создать билет с этим человеком, поэтому нужно отловить ошибку на уровне базы данных, которая будет выброшена при попытке удалить человека, который используется в билетах
    @Transactional(Transactional.TxType.REQUIRED)
    public void deletePersonById(Long personId){
        log.info("Удаление человека: personId={}", personId);
        if (personRepository.findById(personId)!=null) {
            long count = ticketStatisticsService.countByPerson(personId);
            if (count > 0) throw new PersonConflictException("Человек с id " + personId + " используется в билетах; количество: " + count);
            try {
                personRepository.deleteById(personId);
            } catch (ConstraintViolationException e) {
                if (PSQLState.FOREIGN_KEY_VIOLATION.getState().equals(e.getSQLState())) {
                    throw new PersonConflictException("Человек с id " + personId + " используется в билетах");
                }
                throw e;
            log.info("Запрос на удаление человека выполнен: personId={}", personId);
        }else{
            throw new ResourceNotFoundException("Человек с id " + personId + " не найден");
        }
    }
    @Transactional(Transactional.TxType.REQUIRED)
    public PersonResponseDto updatePerson(Long personId, PersonCreateDto personCreateDto) {
        log.info("Обновление человека: personId={}", personId);
        Person person = personRepository.findById(personId);
        personMapper.updatePersonFromDto(personCreateDto, person);
        try {
            personRepository.update(person);
        } catch (OptimisticLockException e) {
            if (e.getCause() instanceof StaleObjectStateException stale
                    && Person.class.getName().equals(stale.getEntityName())
                    && personId.equals(stale.getIdentifier())) {
                throw new ResourceNotFoundException("Человек с id " + personId + " не найден");
            }
            throw e;
        }
        log.info("Запрос на обновление человека выполнен: personId={}", personId);
        return personMapper.toPersonResponseDto(person);
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public ResponsePaginationDto<PersonResponseDto> search(SearchDto<PersonSearchFilterDto> requestDto) {
        log.info("Поиск людей: страница={}, размер={}", requestDto.getPage(), requestDto.getSize());
        SearchValidation.validateSorts(requestDto.getSort(), Set.of("id", "birthday", "weight", "location.x", "location.y", "location.name"));
        List<Sort<Person>> sorts = new ArrayList<>();
        if (requestDto.getSort() != null) {
            for (var item : requestDto.getSort()) {
                String field = item.field();
                String direction = item.direction();
                if ("DESC".equalsIgnoreCase(direction)) {
                    sorts.add(Sort.desc(field));
                }else{
                    sorts.add(Sort.asc(field));
                }
            }
        }
        if (sorts.stream().noneMatch(s -> s.property().equals("id")))
            sorts.add(Sort.asc("id"));
        PersonSearchFilterDto filter = requestDto.getFilter();
        Long total = personRepository.count(filter);
        long totalPages = total / requestDto.getSize();
        if (total % requestDto.getSize() != 0) {
            totalPages++;
        }
        List<Person> people;
        if (requestDto.getPage() >= totalPages) {
            people = List.of();
        } else {
            PageRequest pageRequest = PageRequest.ofPage(requestDto.getPage() + 1).size(requestDto.getSize().intValue());
            people = personRepository.search(filter, pageRequest, sorts);
        }
        List<PersonResponseDto> content = people.stream().map(person -> personMapper.toPersonResponseDto(person)).toList();
        return ResponsePaginationDto.ans(content,requestDto.getPage(),requestDto.getSize(),total);
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public ResponsePaginationDto<PersonResponseDto> getPersons(long page, int size) {
        return search(new SearchDto<PersonSearchFilterDto>(null, page, (long) size, null));
    }

    public Person getPersonById(Long personId) {
        return personRepository.findById(personId);
    }

    public Person getPersonByIdSerializable(Long personId) {
        return personRepository.findByIdSerializable(personId);
    }
}
