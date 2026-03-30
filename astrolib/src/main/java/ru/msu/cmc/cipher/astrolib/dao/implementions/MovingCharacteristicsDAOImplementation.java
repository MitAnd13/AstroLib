package ru.msu.cmc.cipher.astrolib.dao.implementions;

import jakarta.persistence.NoResultException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.msu.cmc.cipher.astrolib.dao.MovingCharacteristicsDAO;
import ru.msu.cmc.cipher.astrolib.models.AstroObjects;
import ru.msu.cmc.cipher.astrolib.models.MovingCharacteristics;

import java.util.Collection;

@Repository
@Transactional
public class MovingCharacteristicsDAOImplementation extends CommonDAOImplementation<MovingCharacteristics, Long>
        implements MovingCharacteristicsDAO {

    @Override
    public void insertForObject(MovingCharacteristics characteristics) {
        prepareMovingCharacteristics(characteristics);
        entityManager.persist(characteristics);
    }

    @Override
    public void updateForObject(MovingCharacteristics characteristics) {
        prepareMovingCharacteristics(characteristics);
        entityManager.merge(characteristics);
    }

    @Override
    public MovingCharacteristics getByObjectId(Long objectId) {
        try {
            return entityManager.createQuery(
                "FROM MovingCharacteristics mc WHERE mc.object.id = :objectId",
                MovingCharacteristics.class
            )
                .setParameter("objectId", objectId)
                .getSingleResult();
        } catch (NoResultException exception) {
            return null;
        }
    }

    @Override
    public Collection<MovingCharacteristics> getAllWithObjects() {
        return entityManager.createQuery(
            "FROM MovingCharacteristics mc JOIN FETCH mc.object o ORDER BY o.name",
            MovingCharacteristics.class
        ).getResultList();
    }

    @Override
    public Collection<MovingCharacteristics> getByObjectType(AstroObjects.ObjType type) {
        return entityManager.createQuery(
            "FROM MovingCharacteristics mc JOIN FETCH mc.object o WHERE o.type = :type ORDER BY o.name",
            MovingCharacteristics.class
        )
            .setParameter("type", type)
            .getResultList();
    }

    @Override
    public Collection<MovingCharacteristics> getByFilters(
        AstroObjects.ObjType type,
        Long minSemiaxis,
        Long maxSemiaxis,
        Float minEccentricity,
        Float maxEccentricity,
        Float minInclination,
        Float maxInclination,
        Float minLongitudeOfAscAngle,
        Float maxLongitudeOfAscAngle,
        Integer minVelocity,
        Integer maxVelocity,
        Integer minLight,
        Integer maxLight
    ) {
        return entityManager.createQuery(
            "FROM MovingCharacteristics mc JOIN FETCH mc.object o " +
            "WHERE (:type IS NULL OR o.type = :type) " +
            "AND (:minSemiaxis IS NULL OR mc.semiaxis >= :minSemiaxis) " +
            "AND (:maxSemiaxis IS NULL OR mc.semiaxis <= :maxSemiaxis) " +
            "AND (:minEccentricity IS NULL OR mc.eccentricity >= :minEccentricity) " +
            "AND (:maxEccentricity IS NULL OR mc.eccentricity <= :maxEccentricity) " +
            "AND (:minInclination IS NULL OR mc.inclination >= :minInclination) " +
            "AND (:maxInclination IS NULL OR mc.inclination <= :maxInclination) " +
            "AND (:minLongitudeOfAscAngle IS NULL OR mc.longitude_of_asc_angle >= :minLongitudeOfAscAngle) " +
            "AND (:maxLongitudeOfAscAngle IS NULL OR mc.longitude_of_asc_angle <= :maxLongitudeOfAscAngle) " +
            "AND (:minVelocity IS NULL OR mc.min_velocity >= :minVelocity) " +
            "AND (:maxVelocity IS NULL OR mc.max_velocity <= :maxVelocity) " +
            "AND (:minLight IS NULL OR mc.min_light >= :minLight) " +
            "AND (:maxLight IS NULL OR mc.max_light <= :maxLight) " +
            "ORDER BY o.name",
            MovingCharacteristics.class
        )
            .setParameter("type", type)
            .setParameter("minSemiaxis", minSemiaxis)
            .setParameter("maxSemiaxis", maxSemiaxis)
            .setParameter("minEccentricity", minEccentricity)
            .setParameter("maxEccentricity", maxEccentricity)
            .setParameter("minInclination", minInclination)
            .setParameter("maxInclination", maxInclination)
            .setParameter("minLongitudeOfAscAngle", minLongitudeOfAscAngle)
            .setParameter("maxLongitudeOfAscAngle", maxLongitudeOfAscAngle)
            .setParameter("minVelocity", minVelocity)
            .setParameter("maxVelocity", maxVelocity)
            .setParameter("minLight", minLight)
            .setParameter("maxLight", maxLight)
            .getResultList();
    }

    private void prepareMovingCharacteristics(MovingCharacteristics characteristics) {
        if (characteristics == null || characteristics.getObject() == null || characteristics.getObject().getId() == null) {
            throw new IllegalArgumentException("Moving characteristics must reference an existing object");
        }

        AstroObjects managedObject = entityManager.find(AstroObjects.class, characteristics.getObject().getId());
        if (managedObject == null) {
            throw new IllegalArgumentException("Referenced object does not exist");
        }
        if (!isMovingType(managedObject.getType())) {
            throw new IllegalArgumentException("Moving characteristics are allowed only for moving objects");
        }

        characteristics.setObject(managedObject);
        characteristics.setId(managedObject.getId());
    }

    private boolean isMovingType(AstroObjects.ObjType type) {
        return type == AstroObjects.ObjType.PLANET
            || type == AstroObjects.ObjType.SATELLITE
            || type == AstroObjects.ObjType.ASTEROID
            || type == AstroObjects.ObjType.COMET
            || type == AstroObjects.ObjType.METEOR_SHOWER;
    }
}
