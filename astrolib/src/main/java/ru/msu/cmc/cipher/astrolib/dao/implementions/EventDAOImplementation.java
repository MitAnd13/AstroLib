package ru.msu.cmc.cipher.astrolib.dao.implementions;

import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.msu.cmc.cipher.astrolib.dao.EventDAO;
import ru.msu.cmc.cipher.astrolib.models.Events;
import ru.msu.cmc.cipher.astrolib.models.ObjectsToEvents;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;

@Repository
@Transactional
public class EventDAOImplementation extends CommonDAOImplementation<Events, Long> implements EventDAO {

    //поиск по имени
    @Override
    public Events getByName(String name) {
        try {
            return entityManager.createQuery(
                "FROM Events e WHERE lower(e.name) = lower(:name)",
                Events.class
                )
                .setParameter("name", name)
                .getSingleResult();
        } catch (NoResultException exception) {
            return null;
        }
    }

    //проверка существования
    @Override
    public boolean existsByName(String name) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(e) FROM Events e WHERE lower(e.name) = lower(:name)",
            Long.class
        );
        query.setParameter("name", name);
        return query.getSingleResult() > 0;
    }

    //неточный поиск по имени
    @Override
    public Collection<Events> getByNameLike(String namePart) {
        return entityManager.createQuery(
            "FROM Events e WHERE lower(e.name) LIKE lower(:namePart) ORDER BY e.name",
            Events.class
            )
            .setParameter("namePart", "%" + namePart + "%")
            .getResultList();
    }

    //поиск по типу
    @Override
    public Collection<Events> getByType(String type) {
        return entityManager.createQuery(
            "FROM Events e WHERE lower(e.type) = lower(:type) ORDER BY e.start_date, e.name",
            Events.class
            )
            .setParameter("type", type)
            .getResultList();
    }

    //поиск по временному интервалу
    @Override
    public Collection<Events> getByDateRange(LocalDate startDate, LocalDate endDate) {
        return entityManager.createQuery(
            "FROM Events e " +
            "WHERE e.start_date <= :endDate " +
            "AND (e.end_date IS NULL OR e.end_date >= :startDate) " +
            "ORDER BY e.start_date, e.name",
            Events.class
            )
            .setParameter("startDate", startDate)
            .setParameter("endDate", endDate)
            .getResultList();
    }

    //поиск по свзяанным объектам
    @Override
    public Collection<Events> getByObjectId(Long objectId) {
        return entityManager.createQuery(
            "SELECT DISTINCT link.event " +
            "FROM ObjectsToEvents link " +
            "WHERE link.object.id = :objectId " +
            "ORDER BY link.event.start_date, link.event.name",
            Events.class
            )
            .setParameter("objectId", objectId)
            .getResultList();
    }

    //смешанный поиск
    @Override
    public Collection<Events> getByObjectIdAndDateRange(Long objectId, LocalDate startDate, LocalDate endDate) {
        return entityManager.createQuery(
            "SELECT DISTINCT link.event " +
            "FROM ObjectsToEvents link " +
            "WHERE link.object.id = :objectId " +
            "AND link.event.start_date <= :endDate " +
            "AND (link.event.end_date IS NULL OR link.event.end_date >= :startDate) " +
            "ORDER BY link.event.start_date, link.event.name",
            Events.class
            )
            .setParameter("objectId", objectId)
            .setParameter("startDate", startDate)
            .setParameter("endDate", endDate)
            .getResultList();
    }

    // выборка ролей объектов в явлениях
    @Override
    public Collection<ObjectsToEvents> getObjectLinks(Long eventId) {
        return entityManager.createQuery(
            "FROM ObjectsToEvents link " +
            "WHERE link.event.id = :eventId " +
            "ORDER BY link.role, link.object.name",
            ObjectsToEvents.class
            )
            .setParameter("eventId", eventId)
            .getResultList();
    }

    //вставка - для регистрации нового
    @Override
    public void insert(Events event, Collection<ObjectsToEvents> objectLinks) {
        entityManager.persist(event);
        persistLinks(event, objectLinks);
    }

    //обновление - на будущее - для внесения изменений в каталог
    @Override
    public void update(Events event, Collection<ObjectsToEvents> objectLinks) {
        Events managedEvent = entityManager.merge(event);

        entityManager.createQuery(
            "DELETE FROM ObjectsToEvents link WHERE link.event.id = :eventId"
            )
            .setParameter("eventId", managedEvent.getId())
            .executeUpdate();

        persistLinks(managedEvent, objectLinks);
    }

    // запись связей объектов и их ролей в сущность ObjectsToEvents
    private void persistLinks(Events event, Collection<ObjectsToEvents> objectLinks) {
        for (ObjectsToEvents link : safeLinks(objectLinks)) {
            link.setEvent(event);
            entityManager.persist(link);
        }
    }

    private Collection<ObjectsToEvents> safeLinks(Collection<ObjectsToEvents> objectLinks) {
        return objectLinks == null ? Collections.emptyList() : objectLinks;
    }
}
