package ru.msu.cmc.cipher.astrolib.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.msu.cmc.cipher.astrolib.dao.AstroObjectDAO;
import ru.msu.cmc.cipher.astrolib.dao.EventDAO;
import ru.msu.cmc.cipher.astrolib.dao.MovingCharacteristicsDAO;
import ru.msu.cmc.cipher.astrolib.dao.StaticCharacteristicsDAO;
import ru.msu.cmc.cipher.astrolib.forms.EventFilterForm;
import ru.msu.cmc.cipher.astrolib.forms.ObjectFilterForm;
import ru.msu.cmc.cipher.astrolib.models.AstroObjects;
import ru.msu.cmc.cipher.astrolib.models.Events;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock
    private AstroObjectDAO astroObjectDAO;

    @Mock
    private EventDAO eventDAO;

    @Mock
    private StaticCharacteristicsDAO staticCharacteristicsDAO;

    @Mock
    private MovingCharacteristicsDAO movingCharacteristicsDAO;

    @InjectMocks
    private SearchService searchService;

    @Test
    void searchObjectsByFiltersShouldReturnBaseMatchesWhenCharacteristicFiltersAreMissing() {
        AstroObjects sun = new AstroObjects("Sun", AstroObjects.ObjType.STAR);
        sun.setId(1L);

        ObjectFilterForm form = new ObjectFilterForm();
        form.setObjectKind("star");

        when(astroObjectDAO.getStarsByFilters(null, null, null)).thenReturn(List.of(sun));
        when(astroObjectDAO.getById(1L)).thenReturn(sun);

        List<AstroObjects> results = searchService.searchObjectsByFilters(form);

        assertEquals(1, results.size());
        assertEquals("Sun", results.get(0).getName());
        verify(staticCharacteristicsDAO, never()).getByFilters(
            eq(AstroObjects.ObjType.STAR), any(), any(), any(), any(), any(), any(), any()
        );
    }

    @Test
    void searchObjectsByFiltersShouldIntersectWithCharacteristicsWhenTheyAreProvided() {
        AstroObjects sun = new AstroObjects("Sun", AstroObjects.ObjType.STAR);
        sun.setId(1L);

        ObjectFilterForm form = new ObjectFilterForm();
        form.setObjectKind("star");
        form.setConstellation("Orion");

        when(astroObjectDAO.getStarsByFilters(null, null, null)).thenReturn(List.of(sun));
        when(staticCharacteristicsDAO.getByFilters(AstroObjects.ObjType.STAR, null, null, null, null, null, null, "Orion"))
            .thenReturn(List.of());

        List<AstroObjects> results = searchService.searchObjectsByFilters(form);

        assertEquals(0, results.size());
        verify(staticCharacteristicsDAO).getByFilters(AstroObjects.ObjType.STAR, null, null, null, null, null, null, "Orion");
    }

    @Test
    void searchEventsByFiltersShouldFilterByLinkedObjectAndPeriodicity() {
        AstroObjects earth = new AstroObjects("Earth", AstroObjects.ObjType.PLANET);
        earth.setId(10L);

        Events event = new Events("Opposition", "Противостояние");
        event.setId(22L);
        event.setCatalog_id("Уникальное");
        event.setStart_date(LocalDate.of(2026, 5, 1));

        EventFilterForm form = new EventFilterForm();
        form.setLinkedObjectName("Earth");
        form.setPeriodicity("Уникальное");

        when(astroObjectDAO.getByName("Earth")).thenReturn(earth);
        when(eventDAO.getByObjectId(10L)).thenReturn(List.of(event));
        when(eventDAO.getById(22L)).thenReturn(event);

        List<Events> results = searchService.searchEventsByFilters(form);

        assertEquals(1, results.size());
        assertEquals("Opposition", results.get(0).getName());
    }
}
