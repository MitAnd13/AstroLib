package ru.msu.cmc.cipher.astrolib.dao;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.transaction.annotation.Transactional;
import ru.msu.cmc.cipher.astrolib.models.AstroObjects;
import ru.msu.cmc.cipher.astrolib.models.StaticCharacteristics;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class StaticCharacteristicsDAOTest {

    @Autowired
    private AstroObjectDAO astroObjectDAO;

    @Autowired
    private StaticCharacteristicsDAO staticCharacteristicsDAO;

    @Autowired
    private EntityManager entityManager;

    @Test
    void insertForObjectShouldPersistCharacteristicsForStaticObject() {
        AstroObjects star = new AstroObjects("Rigel", AstroObjects.ObjType.STAR);
        star.setStar_spectre('B');
        astroObjectDAO.insertTyped(star);

        StaticCharacteristics characteristics = new StaticCharacteristics(12.5f, -8.2f, 860L, "Orion");
        characteristics.setObject(star);

        staticCharacteristicsDAO.insertForObject(characteristics);
        entityManager.flush();
        entityManager.clear();

        StaticCharacteristics saved = staticCharacteristicsDAO.getByObjectId(star.getId());
        assertNotNull(saved);
        assertEquals(star.getId(), saved.getId());
        assertEquals("Orion", saved.getConstellation());
    }

    @Test
    void updateForObjectShouldChangeStoredValues() {
        AstroObjects galaxy = new AstroObjects("Andromeda", AstroObjects.ObjType.GALAXY);
        galaxy.setGalaxy_type("Spiral");
        astroObjectDAO.insertTyped(galaxy);

        StaticCharacteristics characteristics = new StaticCharacteristics(10.0f, 20.0f, 2500L, "Andromeda");
        characteristics.setObject(galaxy);
        staticCharacteristicsDAO.insertForObject(characteristics);
        entityManager.flush();

        characteristics.setDeclension(21.5f);
        characteristics.setSun_distance(2600L);
        staticCharacteristicsDAO.updateForObject(characteristics);
        entityManager.flush();
        entityManager.clear();

        StaticCharacteristics saved = staticCharacteristicsDAO.getByObjectId(galaxy.getId());
        assertNotNull(saved);
        assertEquals(21.5f, saved.getDeclension());
        assertEquals(2600L, saved.getSun_distance());
    }

    @Test
    void getByFiltersShouldReturnOnlyMatchingStaticObjects() {
        AstroObjects star = new AstroObjects("Vega", AstroObjects.ObjType.STAR);
        star.setStar_spectre('A');
        astroObjectDAO.insertTyped(star);

        AstroObjects nebula = new AstroObjects("Crab", AstroObjects.ObjType.NEBULA);
        nebula.setNebula_type("Supernova remnant");
        astroObjectDAO.insertTyped(nebula);

        StaticCharacteristics starCharacteristics = new StaticCharacteristics(18.6f, 38.8f, 25L, "Lyra");
        starCharacteristics.setObject(star);
        staticCharacteristicsDAO.insertForObject(starCharacteristics);

        StaticCharacteristics nebulaCharacteristics = new StaticCharacteristics(5.5f, 22.0f, 6500L, "Taurus");
        nebulaCharacteristics.setObject(nebula);
        staticCharacteristicsDAO.insertForObject(nebulaCharacteristics);

        entityManager.flush();
        entityManager.clear();

        Collection<StaticCharacteristics> filtered = staticCharacteristicsDAO.getByFilters(
            AstroObjects.ObjType.STAR, 18.0f, 19.0f, 30.0f, 40.0f, null, null, "Lyra"
        );

        assertEquals(1, filtered.size());
        assertEquals("Vega", filtered.iterator().next().getObject().getName());
    }

    @Test
    void insertForObjectShouldRejectMovingObject() {
        AstroObjects satellite = new AstroObjects("Europa", AstroObjects.ObjType.SATELLITE);
        satellite.setSatellite_type("Natural");
        astroObjectDAO.insertTyped(satellite);

        StaticCharacteristics characteristics = new StaticCharacteristics(1.0f, 2.0f, 3L, "Jupiter");
        characteristics.setObject(satellite);

        InvalidDataAccessApiUsageException exception = assertThrows(
            InvalidDataAccessApiUsageException.class,
            () -> staticCharacteristicsDAO.insertForObject(characteristics)
        );
        assertEquals("Static characteristics are allowed only for static objects", exception.getMessage());
    }

    @Test
    void commonReadMethodsShouldWorkForStaticCharacteristics() {
        AstroObjects star = new AstroObjects("Altair", AstroObjects.ObjType.STAR);
        star.setStar_spectre('A');
        astroObjectDAO.insertTyped(star);

        StaticCharacteristics characteristics = new StaticCharacteristics(19.8f, 8.9f, 17L, "Aquila");
        characteristics.setObject(star);
        staticCharacteristicsDAO.insertForObject(characteristics);
        entityManager.flush();
        entityManager.clear();

        StaticCharacteristics byId = staticCharacteristicsDAO.getById(star.getId());
        Collection<StaticCharacteristics> all = staticCharacteristicsDAO.getAll();
        Collection<StaticCharacteristics> allWithObjects = staticCharacteristicsDAO.getAllWithObjects();
        Collection<StaticCharacteristics> byType = staticCharacteristicsDAO.getByObjectType(AstroObjects.ObjType.STAR);

        assertNotNull(byId);
        assertEquals(1, all.size());
        assertEquals(1, allWithObjects.size());
        assertEquals(1, byType.size());

        staticCharacteristicsDAO.deleteById(star.getId());
        entityManager.flush();
        entityManager.clear();

        assertEquals(0, staticCharacteristicsDAO.getAll().size());
    }
}
