package ru.msu.cmc.cipher.astrolib.dao;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.transaction.annotation.Transactional;
import ru.msu.cmc.cipher.astrolib.models.AstroObjects;
import ru.msu.cmc.cipher.astrolib.models.MovingCharacteristics;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class MovingCharacteristicsDAOTest {

    @Autowired
    private AstroObjectDAO astroObjectDAO;

    @Autowired
    private MovingCharacteristicsDAO movingCharacteristicsDAO;

    @Autowired
    private EntityManager entityManager;

    @Test
    void insertForObjectShouldPersistCharacteristicsForMovingObject() {
        AstroObjects planet = new AstroObjects("Mars", AstroObjects.ObjType.PLANET);
        planet.setPlanet_type("Terrestrial");
        astroObjectDAO.insertTyped(planet);

        MovingCharacteristics characteristics = new MovingCharacteristics(10L, 0.2f, 5.0f, 25.0f, 12, 24, -2, 4);
        characteristics.setObject(planet);

        movingCharacteristicsDAO.insertForObject(characteristics);
        entityManager.flush();
        entityManager.clear();

        MovingCharacteristics saved = movingCharacteristicsDAO.getByObjectId(planet.getId());
        assertNotNull(saved);
        assertEquals(planet.getId(), saved.getId());
        assertEquals(planet.getId(), saved.getObject().getId());
        assertEquals(10L, saved.getSemiaxis());
    }

    @Test
    void updateForObjectShouldChangeStoredValues() {
        AstroObjects comet = new AstroObjects("Encke", AstroObjects.ObjType.COMET);
        comet.setComet_type("Short-period");
        astroObjectDAO.insertTyped(comet);

        MovingCharacteristics characteristics = new MovingCharacteristics(12L, 0.4f, 7.0f, 30.0f, 10, 20, 1, 7);
        characteristics.setObject(comet);
        movingCharacteristicsDAO.insertForObject(characteristics);
        entityManager.flush();

        characteristics.setSemiaxis(20L);
        characteristics.setMax_velocity(40);
        movingCharacteristicsDAO.updateForObject(characteristics);
        entityManager.flush();
        entityManager.clear();

        MovingCharacteristics saved = movingCharacteristicsDAO.getByObjectId(comet.getId());
        assertNotNull(saved);
        assertEquals(20L, saved.getSemiaxis());
        assertEquals(40, saved.getMax_velocity());
    }

    @Test
    void getByFiltersShouldReturnOnlyMatchingMovingObjects() {
        AstroObjects asteroid = new AstroObjects("Vesta", AstroObjects.ObjType.ASTEROID);
        asteroid.setAsteroid_group("Main belt");
        astroObjectDAO.insertTyped(asteroid);

        AstroObjects comet = new AstroObjects("Swift-Tuttle", AstroObjects.ObjType.COMET);
        comet.setComet_type("Periodic");
        astroObjectDAO.insertTyped(comet);

        MovingCharacteristics asteroidCharacteristics = new MovingCharacteristics(15L, 0.1f, 3.0f, 12.0f, 9, 14, 2, 5);
        asteroidCharacteristics.setObject(asteroid);
        movingCharacteristicsDAO.insertForObject(asteroidCharacteristics);

        MovingCharacteristics cometCharacteristics = new MovingCharacteristics(50L, 0.7f, 11.0f, 60.0f, 20, 30, -1, 8);
        cometCharacteristics.setObject(comet);
        movingCharacteristicsDAO.insertForObject(cometCharacteristics);

        entityManager.flush();
        entityManager.clear();

        Collection<MovingCharacteristics> filtered = movingCharacteristicsDAO.getByFilters(
            AstroObjects.ObjType.ASTEROID, 10L, 20L, 0.0f, 0.2f, null, null, null, null, null, null, null, null
        );

        assertEquals(1, filtered.size());
        assertEquals("Vesta", filtered.iterator().next().getObject().getName());
    }

    @Test
    void insertForObjectShouldRejectStaticObject() {
        AstroObjects star = new AstroObjects("Polaris", AstroObjects.ObjType.STAR);
        astroObjectDAO.insertTyped(star);

        MovingCharacteristics characteristics = new MovingCharacteristics(11L, 0.3f, 4.0f, 20.0f, 8, 16, 0, 3);
        characteristics.setObject(star);

        InvalidDataAccessApiUsageException exception = assertThrows(
            InvalidDataAccessApiUsageException.class,
            () -> movingCharacteristicsDAO.insertForObject(characteristics)
        );
        assertEquals("Moving characteristics are allowed only for moving objects", exception.getMessage());
    }

    @Test
    void commonReadMethodsShouldWorkForMovingCharacteristics() {
        AstroObjects comet = new AstroObjects("Tempel", AstroObjects.ObjType.COMET);
        comet.setComet_type("Periodic");
        astroObjectDAO.insertTyped(comet);

        MovingCharacteristics characteristics = new MovingCharacteristics(18L, 0.5f, 9.0f, 40.0f, 11, 19, 0, 6);
        characteristics.setObject(comet);
        movingCharacteristicsDAO.insertForObject(characteristics);
        entityManager.flush();
        entityManager.clear();

        MovingCharacteristics byId = movingCharacteristicsDAO.getById(comet.getId());
        Collection<MovingCharacteristics> all = movingCharacteristicsDAO.getAll();
        Collection<MovingCharacteristics> allWithObjects = movingCharacteristicsDAO.getAllWithObjects();
        Collection<MovingCharacteristics> byType = movingCharacteristicsDAO.getByObjectType(AstroObjects.ObjType.COMET);

        assertNotNull(byId);
        assertEquals(1, all.size());
        assertEquals(1, allWithObjects.size());
        assertEquals(1, byType.size());

        movingCharacteristicsDAO.deleteById(comet.getId());
        entityManager.flush();
        entityManager.clear();

        assertEquals(0, movingCharacteristicsDAO.getAll().size());
    }

    @Test
    void emptyAndInvalidMovingCharacteristicsBranchesShouldWork() {
        assertEquals(0, movingCharacteristicsDAO.getByFilters(
            AstroObjects.ObjType.COMET, 999L, 1000L, null, null, null, null, null, null, null, null, null, null
        ).size());
        assertEquals(0, movingCharacteristicsDAO.getByObjectType(AstroObjects.ObjType.COMET).size());
        assertEquals(0, movingCharacteristicsDAO.getAllWithObjects().size());
        assertEquals(0, movingCharacteristicsDAO.getAll().size());
        assertEquals(null, movingCharacteristicsDAO.getByObjectId(12345L));

        InvalidDataAccessApiUsageException nullReferenceException = assertThrows(
            InvalidDataAccessApiUsageException.class,
            () -> movingCharacteristicsDAO.insertForObject(new MovingCharacteristics(1L, 0.1f, 1.0f, 1.0f, 1, 1, 1, 1))
        );
        assertEquals("Moving characteristics must reference an existing object", nullReferenceException.getMessage());

        AstroObjects missingObject = new AstroObjects("MissingMoving", AstroObjects.ObjType.COMET);
        missingObject.setId(99999L);
        MovingCharacteristics missingCharacteristics = new MovingCharacteristics(1L, 0.1f, 1.0f, 1.0f, 1, 1, 1, 1);
        missingCharacteristics.setObject(missingObject);

        InvalidDataAccessApiUsageException missingObjectException = assertThrows(
            InvalidDataAccessApiUsageException.class,
            () -> movingCharacteristicsDAO.insertForObject(missingCharacteristics)
        );
        assertEquals("Referenced object does not exist", missingObjectException.getMessage());
    }

    @Test
    void movingCharacteristicsShouldCoverNullTypeAndNullIdBranches() {
        AstroObjects comet = new AstroObjects("BranchComet", AstroObjects.ObjType.COMET);
        comet.setComet_type("Periodic");
        astroObjectDAO.insertTyped(comet);

        MovingCharacteristics characteristics = new MovingCharacteristics(14L, 0.3f, 2.0f, 15.0f, 5, 10, 1, 2);
        characteristics.setObject(comet);
        movingCharacteristicsDAO.insertForObject(characteristics);
        entityManager.flush();
        entityManager.clear();

        assertEquals(1, movingCharacteristicsDAO.getByFilters(
            null, null, null, null, null, null, null, null, null, null, null, null, null
        ).size());

        MovingCharacteristics nullObjectCharacteristics = new MovingCharacteristics(1L, 0.1f, 1.0f, 1.0f, 1, 1, 1, 1);
        InvalidDataAccessApiUsageException nullObjectException = assertThrows(
            InvalidDataAccessApiUsageException.class,
            () -> movingCharacteristicsDAO.insertForObject(nullObjectCharacteristics)
        );
        assertEquals("Moving characteristics must reference an existing object", nullObjectException.getMessage());

        AstroObjects objectWithoutId = new AstroObjects("NoIdMoving", AstroObjects.ObjType.COMET);
        MovingCharacteristics noIdCharacteristics = new MovingCharacteristics(1L, 0.1f, 1.0f, 1.0f, 1, 1, 1, 1);
        noIdCharacteristics.setObject(objectWithoutId);

        InvalidDataAccessApiUsageException noIdException = assertThrows(
            InvalidDataAccessApiUsageException.class,
            () -> movingCharacteristicsDAO.insertForObject(noIdCharacteristics)
        );
        assertEquals("Moving characteristics must reference an existing object", noIdException.getMessage());
        assertNull(movingCharacteristicsDAO.getByObjectId(77777L));
    }

    @Test
    void movingCharacteristicsShouldRejectNullCharacteristicsAndSupportOtherMovingTypes() {
        InvalidDataAccessApiUsageException nullCharacteristicsException = assertThrows(
            InvalidDataAccessApiUsageException.class,
            () -> movingCharacteristicsDAO.insertForObject(null)
        );
        assertEquals("Moving characteristics must reference an existing object", nullCharacteristicsException.getMessage());

        AstroObjects satellite = new AstroObjects("CoverageSatellite", AstroObjects.ObjType.SATELLITE);
        satellite.setSatellite_type("Natural");
        astroObjectDAO.insertTyped(satellite);

        MovingCharacteristics satelliteCharacteristics = new MovingCharacteristics(21L, 0.2f, 4.0f, 11.0f, 3, 7, 1, 2);
        satelliteCharacteristics.setObject(satellite);
        movingCharacteristicsDAO.insertForObject(satelliteCharacteristics);

        AstroObjects meteor = new AstroObjects("CoverageMeteor", AstroObjects.ObjType.METEOR_SHOWER);
        meteor.setMeteor_shower_intensity("Medium");
        astroObjectDAO.insertTyped(meteor);

        MovingCharacteristics meteorCharacteristics = new MovingCharacteristics(31L, 0.4f, 6.0f, 22.0f, 4, 9, 0, 1);
        meteorCharacteristics.setObject(meteor);
        movingCharacteristicsDAO.insertForObject(meteorCharacteristics);

        entityManager.flush();
        entityManager.clear();

        assertEquals(1, movingCharacteristicsDAO.getByObjectType(AstroObjects.ObjType.SATELLITE).size());
        assertEquals(1, movingCharacteristicsDAO.getByObjectType(AstroObjects.ObjType.METEOR_SHOWER).size());
    }
}
