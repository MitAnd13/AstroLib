package ru.msu.cmc.cipher.astrolib.dao.implementions;

import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.msu.cmc.cipher.astrolib.dao.AstroObjectDAO;
import ru.msu.cmc.cipher.astrolib.models.AstroObjects;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

@Repository
@Transactional
public class AstroObjectDAOImplementation extends CommonDAOImplementation<AstroObjects, Long> implements AstroObjectDAO {

    private static final List<AstroObjects.ObjType> MOVING_TYPES = List.of(
        AstroObjects.ObjType.PLANET,
        AstroObjects.ObjType.SATELLITE,
        AstroObjects.ObjType.ASTEROID,
        AstroObjects.ObjType.COMET,
        AstroObjects.ObjType.METEOR_SHOWER
    );

    private static final List<AstroObjects.ObjType> STATIC_TYPES = List.of(
        AstroObjects.ObjType.STAR,
        AstroObjects.ObjType.NEBULA,
        AstroObjects.ObjType.GALAXY
    );

    @Override
    public void insertTyped(AstroObjects object) {
        prepareTypedObject(object);
        entityManager.persist(object);
    }

    @Override
    public void updateTyped(AstroObjects object) {
        prepareTypedObject(object);
        entityManager.merge(object);
    }

    @Override
    public Collection<AstroObjects> getMovingObjects() {
        return entityManager.createQuery(
            "FROM AstroObjects o WHERE o.type IN :types ORDER BY o.name",
            AstroObjects.class
        )
            .setParameter("types", MOVING_TYPES)
            .getResultList();
    }

    @Override
    public Collection<AstroObjects> getStaticObjects() {
        return entityManager.createQuery(
            "FROM AstroObjects o WHERE o.type IN :types ORDER BY o.name",
            AstroObjects.class
        )
            .setParameter("types", STATIC_TYPES)
            .getResultList();
    }

    @Override
    public AstroObjects getByName(String name) {
        String normalizedName = normalizeLower(name);
        try {
            return entityManager.createQuery(
                "FROM AstroObjects o WHERE lower(o.name) = :name",
                AstroObjects.class
            )
                .setParameter("name", normalizedName)
                .getSingleResult();
        } catch (NoResultException exception) {
            return null;
        }
    }

    @Override
    public boolean existsByName(String name) {
        String normalizedName = normalizeLower(name);
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(o) FROM AstroObjects o WHERE lower(o.name) = :name",
            Long.class
        );
        query.setParameter("name", normalizedName);
        return query.getSingleResult() > 0;
    }

    @Override
    public Collection<AstroObjects> getByNameLike(String namePart) {
        String normalizedNamePart = "%" + normalizeLower(namePart) + "%";
        return entityManager.createQuery(
            "FROM AstroObjects o WHERE lower(o.name) LIKE :namePart ORDER BY o.name",
            AstroObjects.class
        )
            .setParameter("namePart", normalizedNamePart)
            .getResultList();
    }

    @Override
    public Collection<AstroObjects> getByType(AstroObjects.ObjType type) {
        return entityManager.createQuery(
            "FROM AstroObjects o WHERE o.type = :type ORDER BY o.name",
            AstroObjects.class
        )
            .setParameter("type", type)
            .getResultList();
    }

    @Override
    public Collection<AstroObjects> getStarsByFilters(Character spectre, String light, Integer starCount) {
        String normalizedLight = normalizeLower(light);
        return entityManager.createQuery(
            "FROM AstroObjects o " +
            "WHERE o.type = :type " +
            "AND (:spectre IS NULL OR o.star_spectre = :spectre) " +
            "AND (:light IS NULL OR lower(o.star_light) = :light) " +
            "AND (:starCount IS NULL OR o.star_count = :starCount) " +
            "ORDER BY o.name",
            AstroObjects.class
        )
            .setParameter("type", AstroObjects.ObjType.STAR)
            .setParameter("spectre", spectre)
            .setParameter("light", normalizedLight)
            .setParameter("starCount", starCount)
            .getResultList();
    }

    @Override
    public Collection<AstroObjects> getNebulaeByFilters(String nebulaType) {
        String normalizedNebulaType = normalizeLower(nebulaType);
        return entityManager.createQuery(
            "FROM AstroObjects o " +
            "WHERE o.type = :type " +
            "AND (:nebulaType IS NULL OR lower(o.nebula_type) = :nebulaType) " +
            "ORDER BY o.name",
            AstroObjects.class
        )
            .setParameter("type", AstroObjects.ObjType.NEBULA)
            .setParameter("nebulaType", normalizedNebulaType)
            .getResultList();
    }

    @Override
    public Collection<AstroObjects> getGalaxiesByFilters(String galaxyType) {
        String normalizedGalaxyType = normalizeLower(galaxyType);
        return entityManager.createQuery(
            "FROM AstroObjects o " +
            "WHERE o.type = :type " +
            "AND (:galaxyType IS NULL OR lower(o.galaxy_type) = :galaxyType) " +
            "ORDER BY o.name",
            AstroObjects.class
        )
            .setParameter("type", AstroObjects.ObjType.GALAXY)
            .setParameter("galaxyType", normalizedGalaxyType)
            .getResultList();
    }

    @Override
    public Collection<AstroObjects> getPlanetsByFilters(String planetType, Long parentStarId) {
        String normalizedPlanetType = normalizeLower(planetType);
        return entityManager.createQuery(
            "FROM AstroObjects o " +
            "WHERE o.type = :type " +
            "AND (:planetType IS NULL OR lower(o.planet_type) = :planetType) " +
            "AND (:parentStarId IS NULL OR o.planet_parent_star.id = :parentStarId) " +
            "ORDER BY o.name",
            AstroObjects.class
        )
            .setParameter("type", AstroObjects.ObjType.PLANET)
            .setParameter("planetType", normalizedPlanetType)
            .setParameter("parentStarId", parentStarId)
            .getResultList();
    }

    @Override
    public Collection<AstroObjects> getSatellitesByFilters(String satelliteType, Long parentPlanetId) {
        String normalizedSatelliteType = normalizeLower(satelliteType);
        return entityManager.createQuery(
            "FROM AstroObjects o " +
            "WHERE o.type = :type " +
            "AND (:satelliteType IS NULL OR lower(o.satellite_type) = :satelliteType) " +
            "AND (:parentPlanetId IS NULL OR o.satellite_parent_planet.id = :parentPlanetId) " +
            "ORDER BY o.name",
            AstroObjects.class
        )
            .setParameter("type", AstroObjects.ObjType.SATELLITE)
            .setParameter("satelliteType", normalizedSatelliteType)
            .setParameter("parentPlanetId", parentPlanetId)
            .getResultList();
    }

    @Override
    public Collection<AstroObjects> getAsteroidsByFilters(String asteroidSpectre, String asteroidGroup) {
        String normalizedAsteroidSpectre = normalizeLower(asteroidSpectre);
        String normalizedAsteroidGroup = normalizeLower(asteroidGroup);
        return entityManager.createQuery(
            "FROM AstroObjects o " +
            "WHERE o.type = :type " +
            "AND (:asteroidSpectre IS NULL OR lower(o.asteroid_spectre) = :asteroidSpectre) " +
            "AND (:asteroidGroup IS NULL OR lower(o.asteroid_group) = :asteroidGroup) " +
            "ORDER BY o.name",
            AstroObjects.class
        )
            .setParameter("type", AstroObjects.ObjType.ASTEROID)
            .setParameter("asteroidSpectre", normalizedAsteroidSpectre)
            .setParameter("asteroidGroup", normalizedAsteroidGroup)
            .getResultList();
    }

    @Override
    public Collection<AstroObjects> getCometsByFilters(String cometType, String cometClass) {
        String normalizedCometType = normalizeLower(cometType);
        String normalizedCometClass = normalizeLower(cometClass);
        return entityManager.createQuery(
            "FROM AstroObjects o " +
            "WHERE o.type = :type " +
            "AND (:cometType IS NULL OR lower(o.comet_type) = :cometType) " +
            "AND (:cometClass IS NULL OR lower(o.comet_class) = :cometClass) " +
            "ORDER BY o.name",
            AstroObjects.class
        )
            .setParameter("type", AstroObjects.ObjType.COMET)
            .setParameter("cometType", normalizedCometType)
            .setParameter("cometClass", normalizedCometClass)
            .getResultList();
    }

    @Override
    public Collection<AstroObjects> getMeteorShowersByFilters(String intensity, Long parentCometId) {
        String normalizedIntensity = normalizeLower(intensity);
        return entityManager.createQuery(
            "FROM AstroObjects o " +
            "WHERE o.type = :type " +
            "AND (:intensity IS NULL OR lower(o.meteor_shower_intensity) = :intensity) " +
            "AND (:parentCometId IS NULL OR o.meteor_shower_parent.id = :parentCometId) " +
            "ORDER BY o.name",
            AstroObjects.class
        )
            .setParameter("type", AstroObjects.ObjType.METEOR_SHOWER)
            .setParameter("intensity", normalizedIntensity)
            .setParameter("parentCometId", parentCometId)
            .getResultList();
    }

    private String normalizeLower(String value) {
        return value == null ? null : value.toLowerCase(Locale.ROOT);
    }

    private void prepareTypedObject(AstroObjects object) {
        if (object == null) {
            throw new IllegalArgumentException("Object must not be null");
        }
        if (object.getType() == null) {
            throw new IllegalArgumentException("Object type must not be null");
        }

        AstroObjects parentStar = resolveParent(object.getPlanet_parent_star(), AstroObjects.ObjType.STAR, "Planet parent must be a star");
        AstroObjects parentPlanet = resolveParent(object.getSatellite_parent_planet(), AstroObjects.ObjType.PLANET, "Satellite parent must be a planet");
        AstroObjects parentComet = resolveParent(object.getMeteor_shower_parent(), AstroObjects.ObjType.COMET, "Meteor shower parent must be a comet");

        clearTypeSpecificFields(object);

        switch (object.getType()) {
            case STAR -> {
                // keep only star fields
            }
            case NEBULA -> {
                // keep only nebula fields
            }
            case GALAXY -> {
                // keep only galaxy fields
            }
            case PLANET -> object.setPlanet_parent_star(parentStar);
            case SATELLITE -> object.setSatellite_parent_planet(parentPlanet);
            case ASTEROID -> {
                // keep only asteroid fields
            }
            case COMET -> {
                // keep only comet fields
            }
            case METEOR_SHOWER -> object.setMeteor_shower_parent(parentComet);
        }
    }

    private AstroObjects resolveParent(AstroObjects parent, AstroObjects.ObjType expectedType, String message) {
        if (parent == null) {
            return null;
        }
        if (parent.getId() == null) {
            throw new IllegalArgumentException(message);
        }

        AstroObjects managedParent = entityManager.find(AstroObjects.class, parent.getId());
        if (managedParent == null || managedParent.getType() != expectedType) {
            throw new IllegalArgumentException(message);
        }
        return managedParent;
    }

    private void clearTypeSpecificFields(AstroObjects object) {
        AstroObjects.ObjType type = object.getType();

        if (type != AstroObjects.ObjType.STAR) {
            object.setStar_spectre(null);
            object.setStar_light(null);
            object.setStar_count(null);
        }
        if (type != AstroObjects.ObjType.NEBULA) {
            object.setNebula_type(null);
        }
        if (type != AstroObjects.ObjType.GALAXY) {
            object.setGalaxy_type(null);
        }
        if (type != AstroObjects.ObjType.PLANET) {
            object.setPlanet_type(null);
            object.setPlanet_parent_star(null);
        }
        if (type != AstroObjects.ObjType.SATELLITE) {
            object.setSatellite_type(null);
            object.setSatellite_parent_planet(null);
        }
        if (type != AstroObjects.ObjType.ASTEROID) {
            object.setAsteroid_spectre(null);
            object.setAsteroid_group(null);
        }
        if (type != AstroObjects.ObjType.COMET) {
            object.setComet_type(null);
            object.setComet_class(null);
        }
        if (type != AstroObjects.ObjType.METEOR_SHOWER) {
            object.setMeteor_shower_intensity(null);
            object.setMeteor_shower_parent(null);
        }
    }
}
