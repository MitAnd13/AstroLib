package ru.msu.cmc.cipher.astrolib.dao;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.msu.cmc.cipher.astrolib.models.AstroObjects;
import ru.msu.cmc.cipher.astrolib.models.Events;
import ru.msu.cmc.cipher.astrolib.models.ObjectsToEvents;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class EventDAOTest {

    @Autowired
    private AstroObjectDAO astroObjectDAO;

    @Autowired
    private EventDAO eventDAO;

    @Autowired
    private EntityManager entityManager;

    @Test
    void insertShouldPersistEventAndLinks() {
        AstroObjects sun = new AstroObjects("Sun-event", AstroObjects.ObjType.STAR);
        astroObjectDAO.insertTyped(sun);

        Events event = new Events("Solar eclipse 2026", "Eclipse");
        event.setStart_date(LocalDate.of(2026, 8, 12));
        event.setEnd_date(LocalDate.of(2026, 8, 12));

        ObjectsToEvents link = new ObjectsToEvents(sun, event, "main");
        eventDAO.insert(event, List.of(link));
        entityManager.flush();
        entityManager.clear();

        Events saved = eventDAO.getByName("Solar eclipse 2026");
        assertNotNull(saved);
        assertTrue(eventDAO.existsByName("Solar eclipse 2026"));
        assertEquals(1, eventDAO.getObjectLinks(saved.getId()).size());
        assertEquals(1, eventDAO.getByObjectId(sun.getId()).size());
    }

    @Test
    void updateShouldReplaceLinks() {
        AstroObjects firstObject = new AstroObjects("Moon-event", AstroObjects.ObjType.SATELLITE);
        firstObject.setSatellite_type("Natural");
        astroObjectDAO.insertTyped(firstObject);

        AstroObjects secondObject = new AstroObjects("Comet-event", AstroObjects.ObjType.COMET);
        secondObject.setComet_type("Periodic");
        astroObjectDAO.insertTyped(secondObject);

        Events event = new Events("Meteor watch", "Observation");
        event.setStart_date(LocalDate.of(2026, 10, 1));
        event.setEnd_date(LocalDate.of(2026, 10, 2));

        eventDAO.insert(event, List.of(new ObjectsToEvents(firstObject, event, "old-role")));
        entityManager.flush();

        event.setType("Updated observation");
        eventDAO.update(event, List.of(new ObjectsToEvents(secondObject, event, "new-role")));
        entityManager.flush();
        entityManager.clear();

        Events saved = eventDAO.getByName("Meteor watch");
        assertNotNull(saved);
        assertEquals("Updated observation", saved.getType());
        assertEquals(1, eventDAO.getObjectLinks(saved.getId()).size());
        assertEquals("new-role", eventDAO.getObjectLinks(saved.getId()).iterator().next().getRole());
        assertEquals(0, eventDAO.getByObjectId(firstObject.getId()).size());
        assertEquals(1, eventDAO.getByObjectId(secondObject.getId()).size());
    }

    @Test
    void filtersShouldReturnMatchingEvents() {
        AstroObjects object = new AstroObjects("Venus-event", AstroObjects.ObjType.PLANET);
        object.setPlanet_type("Terrestrial");
        astroObjectDAO.insertTyped(object);

        Events conjunction = new Events("Venus conjunction", "Conjunction");
        conjunction.setStart_date(LocalDate.of(2026, 5, 1));
        conjunction.setEnd_date(LocalDate.of(2026, 5, 3));

        Events occultation = new Events("Venus occultation", "Occultation");
        occultation.setStart_date(LocalDate.of(2026, 6, 10));
        occultation.setEnd_date(LocalDate.of(2026, 6, 10));

        eventDAO.insert(conjunction, List.of(new ObjectsToEvents(object, conjunction, "participant")));
        eventDAO.insert(occultation, List.of(new ObjectsToEvents(object, occultation, "participant")));
        entityManager.flush();
        entityManager.clear();

        assertEquals(2, eventDAO.getByNameLike("Venus").size());
        assertEquals(1, eventDAO.getByType("Conjunction").size());
        assertEquals(1, eventDAO.getByObjectIdAndDateRange(
            object.getId(), LocalDate.of(2026, 5, 2), LocalDate.of(2026, 5, 2)
        ).size());
        assertEquals(2, eventDAO.getByDateRange(LocalDate.of(2026, 5, 1), LocalDate.of(2026, 6, 30)).size());
    }

    @Test
    void emptyAndNullLinkBranchesShouldWork() {
        assertNull(eventDAO.getByName("Missing event"));
        assertFalse(eventDAO.existsByName("Missing event"));
        assertEquals(0, eventDAO.getByNameLike("Missing").size());
        assertEquals(0, eventDAO.getByType("Missing").size());
        assertEquals(0, eventDAO.getByDateRange(LocalDate.of(2030, 1, 1), LocalDate.of(2030, 1, 2)).size());
        assertEquals(0, eventDAO.getByObjectId(99999L).size());
        assertEquals(0, eventDAO.getByObjectIdAndDateRange(99999L, LocalDate.of(2030, 1, 1), LocalDate.of(2030, 1, 2)).size());
        assertEquals(0, eventDAO.getObjectLinks(99999L).size());

        Events event = new Events("Empty links event", "Test");
        event.setStart_date(LocalDate.of(2026, 1, 1));
        event.setEnd_date(LocalDate.of(2026, 1, 1));
        eventDAO.insert(event, null);
        entityManager.flush();
        entityManager.clear();

        Events saved = eventDAO.getByName("Empty links event");
        assertNotNull(saved);
        assertEquals(0, eventDAO.getObjectLinks(saved.getId()).size());

        saved.setType("Updated test");
        eventDAO.update(saved, null);
        entityManager.flush();
        entityManager.clear();

        assertEquals("Updated test", eventDAO.getByName("Empty links event").getType());
    }
}
