package ru.msu.cmc.cipher.astrolib.dao;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.msu.cmc.cipher.astrolib.models.AstroObjects;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class AstroObjectDAOTest {

    @Autowired
    private AstroObjectDAO astroObjectDAO;

    @Autowired
    private EntityManager entityManager;

    @Test
    void insertTypedShouldPersistPlanetWithStarParent() {
        AstroObjects star = new AstroObjects("Sun", AstroObjects.ObjType.STAR);
        astroObjectDAO.insertTyped(star);

        AstroObjects planet = new AstroObjects("Earth", AstroObjects.ObjType.PLANET);
        planet.setPlanet_type("Terrestrial");
        planet.setPlanet_parent_star(star);
        planet.setGalaxy_type("Should be removed");

        astroObjectDAO.insertTyped(planet);
        entityManager.flush();
        entityManager.clear();

        AstroObjects saved = astroObjectDAO.getByName("Earth");
        assertNotNull(saved);
        assertEquals(AstroObjects.ObjType.PLANET, saved.getType());
        assertEquals("Terrestrial", saved.getPlanet_type());
        assertNotNull(saved.getPlanet_parent_star());
        assertEquals(star.getId(), saved.getPlanet_parent_star().getId());
        assertNull(saved.getGalaxy_type());
        assertTrue(astroObjectDAO.existsByName("Earth"));
    }

    @Test
    void updateTypedShouldReplaceIrrelevantFieldsWhenTypeChanges() {
        AstroObjects object = new AstroObjects("Sirius", AstroObjects.ObjType.STAR);
        object.setStar_spectre('A');
        object.setStar_light("Bright");
        object.setStar_count(2);
        astroObjectDAO.insertTyped(object);
        entityManager.flush();

        object.setType(AstroObjects.ObjType.NEBULA);
        object.setNebula_type("Emission");
        astroObjectDAO.updateTyped(object);
        entityManager.flush();
        entityManager.clear();

        AstroObjects saved = astroObjectDAO.getById(object.getId());
        assertNotNull(saved);
        assertEquals(AstroObjects.ObjType.NEBULA, saved.getType());
        assertEquals("Emission", saved.getNebula_type());
        assertNull(saved.getStar_spectre());
        assertNull(saved.getStar_light());
        assertNull(saved.getStar_count());
    }

    @Test
    void getTypeSpecificFiltersShouldReturnOnlyMatchingObjects() {
        AstroObjects firstStar = new AstroObjects("Alpha", AstroObjects.ObjType.STAR);
        firstStar.setStar_spectre('G');
        firstStar.setStar_light("Yellow");
        firstStar.setStar_count(1);

        AstroObjects secondStar = new AstroObjects("Beta", AstroObjects.ObjType.STAR);
        secondStar.setStar_spectre('M');
        secondStar.setStar_light("Red");
        secondStar.setStar_count(1);

        AstroObjects galaxy = new AstroObjects("Milky Way", AstroObjects.ObjType.GALAXY);
        galaxy.setGalaxy_type("Spiral");

        astroObjectDAO.insertTyped(firstStar);
        astroObjectDAO.insertTyped(secondStar);
        astroObjectDAO.insertTyped(galaxy);
        entityManager.flush();
        entityManager.clear();

        Collection<AstroObjects> filteredStars = astroObjectDAO.getStarsByFilters('G', "Yellow", 1);
        Collection<AstroObjects> filteredGalaxies = astroObjectDAO.getGalaxiesByFilters("Spiral");
        Collection<AstroObjects> staticObjects = astroObjectDAO.getStaticObjects();

        assertEquals(1, filteredStars.size());
        assertEquals("Alpha", filteredStars.iterator().next().getName());
        assertEquals(1, filteredGalaxies.size());
        assertEquals("Milky Way", filteredGalaxies.iterator().next().getName());
        assertEquals(3, staticObjects.size());
    }

    @Test
    void insertTypedShouldRejectPlanetWithNonStarParent() {
        AstroObjects comet = new AstroObjects("Halley", AstroObjects.ObjType.COMET);
        astroObjectDAO.insertTyped(comet);

        AstroObjects invalidPlanet = new AstroObjects("FakePlanet", AstroObjects.ObjType.PLANET);
        invalidPlanet.setPlanet_type("Gas giant");
        invalidPlanet.setPlanet_parent_star(comet);

        InvalidDataAccessApiUsageException exception = assertThrows(
            InvalidDataAccessApiUsageException.class,
            () -> astroObjectDAO.insertTyped(invalidPlanet)
        );
        assertEquals("Planet parent must be a star", exception.getMessage());
    }

    @Test
    void commonReadAndDeleteMethodsShouldWork() {
        AstroObjects nebula = new AstroObjects("Orion Nebula", AstroObjects.ObjType.NEBULA);
        nebula.setNebula_type("Diffuse");
        astroObjectDAO.insertTyped(nebula);
        entityManager.flush();
        entityManager.clear();

        Collection<AstroObjects> allObjects = astroObjectDAO.getAll();
        AstroObjects byId = astroObjectDAO.getById(nebula.getId());
        Collection<AstroObjects> byType = astroObjectDAO.getByType(AstroObjects.ObjType.NEBULA);
        Collection<AstroObjects> byNameLike = astroObjectDAO.getByNameLike("Orion");

        assertEquals(1, allObjects.size());
        assertNotNull(byId);
        assertEquals(1, byType.size());
        assertEquals(1, byNameLike.size());

        astroObjectDAO.deleteById(nebula.getId());
        entityManager.flush();
        entityManager.clear();

        assertNull(astroObjectDAO.getById(nebula.getId()));
    }

    @Test
    void movingAndHierarchyFiltersShouldReturnExpectedObjects() {
        AstroObjects star = new AstroObjects("ParentStar", AstroObjects.ObjType.STAR);
        astroObjectDAO.insertTyped(star);

        AstroObjects planet = new AstroObjects("ParentPlanet", AstroObjects.ObjType.PLANET);
        planet.setPlanet_type("Gas giant");
        planet.setPlanet_parent_star(star);
        astroObjectDAO.insertTyped(planet);

        AstroObjects satellite = new AstroObjects("ParentSatellite", AstroObjects.ObjType.SATELLITE);
        satellite.setSatellite_type("Natural");
        satellite.setSatellite_parent_planet(planet);
        astroObjectDAO.insertTyped(satellite);

        AstroObjects asteroid = new AstroObjects("FilterAsteroid", AstroObjects.ObjType.ASTEROID);
        asteroid.setAsteroid_spectre("C");
        asteroid.setAsteroid_group("Main belt");
        astroObjectDAO.insertTyped(asteroid);

        AstroObjects comet = new AstroObjects("FilterComet", AstroObjects.ObjType.COMET);
        comet.setComet_type("Periodic");
        comet.setComet_class("Jupiter-family");
        astroObjectDAO.insertTyped(comet);

        AstroObjects meteor = new AstroObjects("FilterMeteor", AstroObjects.ObjType.METEOR_SHOWER);
        meteor.setMeteor_shower_intensity("High");
        meteor.setMeteor_shower_parent(comet);
        astroObjectDAO.insertTyped(meteor);

        entityManager.flush();
        entityManager.clear();

        assertEquals(5, astroObjectDAO.getMovingObjects().size());
        assertEquals(1, astroObjectDAO.getPlanetsByFilters("Gas giant", star.getId()).size());
        assertEquals(1, astroObjectDAO.getSatellitesByFilters("Natural", planet.getId()).size());
        assertEquals(1, astroObjectDAO.getAsteroidsByFilters("C", "Main belt").size());
        assertEquals(1, astroObjectDAO.getCometsByFilters("Periodic", "Jupiter-family").size());
        assertEquals(1, astroObjectDAO.getMeteorShowersByFilters("High", comet.getId()).size());
    }

    @Test
    void commonCrudMethodsShouldWorkThroughAstroObjectDao() {
        AstroObjects star = new AstroObjects("CommonCrudStar", AstroObjects.ObjType.STAR);
        star.setStar_spectre('F');
        star.setStar_light("White");
        star.setStar_count(1);

        astroObjectDAO.insert(star);
        entityManager.flush();
        entityManager.clear();

        AstroObjects saved = astroObjectDAO.getByName("CommonCrudStar");
        assertNotNull(saved);
        assertEquals("White", saved.getStar_light());

        saved.setStar_light("Blue-white");
        astroObjectDAO.update(saved);
        entityManager.flush();
        entityManager.clear();

        AstroObjects updated = astroObjectDAO.getById(saved.getId());
        assertNotNull(updated);
        assertEquals("Blue-white", updated.getStar_light());

        astroObjectDAO.delete(updated);
        entityManager.flush();
        entityManager.clear();

        assertNull(astroObjectDAO.getById(saved.getId()));
    }

    @Test
    void getNebulaeByFiltersShouldReturnOnlyMatchingNebulae() {
        AstroObjects nebula = new AstroObjects("Lagoon", AstroObjects.ObjType.NEBULA);
        nebula.setNebula_type("Emission");

        AstroObjects secondNebula = new AstroObjects("Helix", AstroObjects.ObjType.NEBULA);
        secondNebula.setNebula_type("Planetary");

        astroObjectDAO.insertTyped(nebula);
        astroObjectDAO.insertTyped(secondNebula);
        entityManager.flush();
        entityManager.clear();

        Collection<AstroObjects> filtered = astroObjectDAO.getNebulaeByFilters("Emission");

        assertEquals(1, filtered.size());
        assertEquals("Lagoon", filtered.iterator().next().getName());
    }
}
