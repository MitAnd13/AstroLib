package ru.msu.cmc.cipher.astrolib.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.msu.cmc.cipher.astrolib.dao.AstroObjectDAO;
import ru.msu.cmc.cipher.astrolib.dao.EventDAO;
import ru.msu.cmc.cipher.astrolib.dao.MovingCharacteristicsDAO;
import ru.msu.cmc.cipher.astrolib.dao.StaticCharacteristicsDAO;
import ru.msu.cmc.cipher.astrolib.forms.DiscoveryForm;
import ru.msu.cmc.cipher.astrolib.models.AstroObjects;
import ru.msu.cmc.cipher.astrolib.models.Events;
import ru.msu.cmc.cipher.astrolib.models.ObjectsToEvents;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiscoveryServiceTest {

    @Mock
    private AstroObjectDAO astroObjectDAO;

    @Mock
    private EventDAO eventDAO;

    @Mock
    private StaticCharacteristicsDAO staticCharacteristicsDAO;

    @Mock
    private MovingCharacteristicsDAO movingCharacteristicsDAO;

    @InjectMocks
    private DiscoveryService discoveryService;

    @Test
    void createDiscoveryShouldPersistEventWithLinks() {
        DiscoveryForm form = new DiscoveryForm();
        form.setDiscoveryKind("event");
        form.setName("Lyrids 2026");
        form.setEventType("Затмение");
        form.setPeriodicity("Уникальное");
        form.setEventStart(LocalDate.of(2026, 4, 21));
        form.setEventEnd(LocalDate.of(2026, 4, 22));
        form.setNotes("Observation window");
        form.setLinkedObjectNames(List.of("Sun"));
        form.setLinkedObjectRoles(List.of("Источник явления"));

        AstroObjects sun = new AstroObjects("Sun", AstroObjects.ObjType.STAR);
        sun.setId(1L);

        when(eventDAO.existsByName(anyString())).thenReturn(false);
        when(astroObjectDAO.getByName("Sun")).thenReturn(sun);

        ArgumentCaptor<Events> eventCaptor = ArgumentCaptor.forClass(Events.class);
        ArgumentCaptor<List<ObjectsToEvents>> linksCaptor = ArgumentCaptor.forClass(List.class);

        discoveryService.createDiscovery(form);

        verify(eventDAO).insert(eventCaptor.capture(), linksCaptor.capture());
        assertEquals("Lyrids 2026", eventCaptor.getValue().getName());
        assertEquals("Затмение", eventCaptor.getValue().getType());
        assertEquals(1, linksCaptor.getValue().size());
        assertEquals("Источник явления", linksCaptor.getValue().get(0).getRole());
        assertEquals(sun, linksCaptor.getValue().get(0).getObject());
    }

    @Test
    void createDiscoveryShouldRejectEventWithoutLinkedObjects() {
        DiscoveryForm form = new DiscoveryForm();
        form.setDiscoveryKind("event");
        form.setName("Empty event");
        form.setEventType("Затмение");
        form.setPeriodicity("Уникальное");
        form.setEventStart(LocalDate.of(2026, 4, 21));
        form.setEventEnd(LocalDate.of(2026, 4, 22));
        form.setNotes("Observation window");

        when(eventDAO.existsByName(anyString())).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> discoveryService.createDiscovery(form)
        );

        assertEquals("Добавьте хотя бы один связанный объект", exception.getMessage());
    }
}
