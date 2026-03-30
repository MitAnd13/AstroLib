package ru.msu.cmc.cipher.astrolib.dao;

import ru.msu.cmc.cipher.astrolib.models.AstroObjects;

import java.util.Collection;

public interface AstroObjectDAO extends CommonDAO<AstroObjects, Long> {

    void insertTyped(AstroObjects object);
    void updateTyped(AstroObjects object);

    Collection<AstroObjects> getMovingObjects();
    Collection<AstroObjects> getStaticObjects();

    AstroObjects getByName(String name);
    boolean existsByName(String name);
    Collection<AstroObjects> getByNameLike(String namePart);
    Collection<AstroObjects> getByType(AstroObjects.ObjType type);

    Collection<AstroObjects> getStarsByFilters(Character spectre, String light, Integer starCount);
    Collection<AstroObjects> getNebulaeByFilters(String nebulaType);
    Collection<AstroObjects> getGalaxiesByFilters(String galaxyType);
    Collection<AstroObjects> getPlanetsByFilters(String planetType, Long parentStarId);
    Collection<AstroObjects> getSatellitesByFilters(String satelliteType, Long parentPlanetId);
    Collection<AstroObjects> getAsteroidsByFilters(String asteroidSpectre, String asteroidGroup);
    Collection<AstroObjects> getCometsByFilters(String cometType, String cometClass);
    Collection<AstroObjects> getMeteorShowersByFilters(String intensity, Long parentCometId);
}
