package ru.msu.cmc.cipher.astrolib.dao;

import ru.msu.cmc.cipher.astrolib.models.Events;
import ru.msu.cmc.cipher.astrolib.models.ObjectsToEvents;

import java.time.LocalDate;
import java.util.Collection;

public interface EventDAO extends CommonDAO<Events, Long> {


    Events getByName(String name);
    boolean existsByName(String name);
    Collection<Events> getByNameLike(String namePart);

    Collection<Events> getByType(String type);
    Collection<Events> getByDateRange(LocalDate startDate, LocalDate endDate);

    Collection<Events> getByObjectId(Long objectId);
    Collection<Events> getByObjectIdAndDateRange(Long objectId, LocalDate startDate, LocalDate endDate);

    Collection<ObjectsToEvents> getObjectLinks(Long eventId);

    void insert(Events event, Collection<ObjectsToEvents> objectLinks);
    void update(Events event, Collection<ObjectsToEvents> objectLinks);
}
