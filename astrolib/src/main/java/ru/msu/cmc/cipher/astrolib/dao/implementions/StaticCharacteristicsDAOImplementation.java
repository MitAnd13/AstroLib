package ru.msu.cmc.cipher.astrolib.dao.implementions;

import jakarta.persistence.NoResultException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.msu.cmc.cipher.astrolib.dao.StaticCharacteristicsDAO;
import ru.msu.cmc.cipher.astrolib.models.AstroObjects;
import ru.msu.cmc.cipher.astrolib.models.StaticCharacteristics;

import java.util.Collection;

@Repository
@Transactional
public class StaticCharacteristicsDAOImplementation extends CommonDAOImplementation<StaticCharacteristics, Long>
        implements StaticCharacteristicsDAO {

    @Override
    public void insertForObject(StaticCharacteristics characteristics) {
        prepareStaticCharacteristics(characteristics);
        entityManager.persist(characteristics);
    }

    @Override
    public void updateForObject(StaticCharacteristics characteristics) {
        prepareStaticCharacteristics(characteristics);
        entityManager.merge(characteristics);
    }

    @Override
    public StaticCharacteristics getByObjectId(Long objectId) {
        try {
            return entityManager.createQuery(
                "FROM StaticCharacteristics sc WHERE sc.object.id = :objectId",
                StaticCharacteristics.class
            )
                .setParameter("objectId", objectId)
                .getSingleResult();
        } catch (NoResultException exception) {
            return null;
        }
    }

    @Override
    public Collection<StaticCharacteristics> getAllWithObjects() {
        return entityManager.createQuery(
            "FROM StaticCharacteristics sc JOIN FETCH sc.object o ORDER BY o.name",
            StaticCharacteristics.class
        ).getResultList();
    }

    @Override
    public Collection<StaticCharacteristics> getByObjectType(AstroObjects.ObjType type) {
        return entityManager.createQuery(
            "FROM StaticCharacteristics sc JOIN FETCH sc.object o WHERE o.type = :type ORDER BY o.name",
            StaticCharacteristics.class
        )
            .setParameter("type", type)
            .getResultList();
    }

    @Override
    public Collection<StaticCharacteristics> getByFilters(
        AstroObjects.ObjType type,
        Float minRightAscension,
        Float maxRightAscension,
        Float minDeclension,
        Float maxDeclension,
        Long minSunDistance,
        Long maxSunDistance,
        String constellation
    ) {
        return entityManager.createQuery(
            "FROM StaticCharacteristics sc JOIN FETCH sc.object o " +
            "WHERE (:type IS NULL OR o.type = :type) " +
            "AND (:minRightAscension IS NULL OR sc.right_ascension >= :minRightAscension) " +
            "AND (:maxRightAscension IS NULL OR sc.right_ascension <= :maxRightAscension) " +
            "AND (:minDeclension IS NULL OR sc.declension >= :minDeclension) " +
            "AND (:maxDeclension IS NULL OR sc.declension <= :maxDeclension) " +
            "AND (:minSunDistance IS NULL OR sc.sun_distance >= :minSunDistance) " +
            "AND (:maxSunDistance IS NULL OR sc.sun_distance <= :maxSunDistance) " +
            "AND (:constellation IS NULL OR lower(sc.constellation) = lower(:constellation)) " +
            "ORDER BY o.name",
            StaticCharacteristics.class
        )
            .setParameter("type", type)
            .setParameter("minRightAscension", minRightAscension)
            .setParameter("maxRightAscension", maxRightAscension)
            .setParameter("minDeclension", minDeclension)
            .setParameter("maxDeclension", maxDeclension)
            .setParameter("minSunDistance", minSunDistance)
            .setParameter("maxSunDistance", maxSunDistance)
            .setParameter("constellation", constellation)
            .getResultList();
    }

    private void prepareStaticCharacteristics(StaticCharacteristics characteristics) {
        if (characteristics == null || characteristics.getObject() == null || characteristics.getObject().getId() == null) {
            throw new IllegalArgumentException("Static characteristics must reference an existing object");
        }

        AstroObjects managedObject = entityManager.find(AstroObjects.class, characteristics.getObject().getId());
        if (managedObject == null) {
            throw new IllegalArgumentException("Referenced object does not exist");
        }
        if (!isStaticType(managedObject.getType())) {
            throw new IllegalArgumentException("Static characteristics are allowed only for static objects");
        }

        characteristics.setObject(managedObject);
        characteristics.setId(managedObject.getId());
    }

    private boolean isStaticType(AstroObjects.ObjType type) {
        return type == AstroObjects.ObjType.STAR
            || type == AstroObjects.ObjType.NEBULA
            || type == AstroObjects.ObjType.GALAXY;
    }
}
