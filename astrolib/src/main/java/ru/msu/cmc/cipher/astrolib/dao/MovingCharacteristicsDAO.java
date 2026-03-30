package ru.msu.cmc.cipher.astrolib.dao;

import ru.msu.cmc.cipher.astrolib.models.AstroObjects;
import ru.msu.cmc.cipher.astrolib.models.MovingCharacteristics;

import java.util.Collection;

public interface MovingCharacteristicsDAO extends CommonDAO<MovingCharacteristics, Long> {

    void insertForObject(MovingCharacteristics characteristics);
    void updateForObject(MovingCharacteristics characteristics);

    MovingCharacteristics getByObjectId(Long objectId);
    Collection<MovingCharacteristics> getAllWithObjects();
    Collection<MovingCharacteristics> getByObjectType(AstroObjects.ObjType type);
    Collection<MovingCharacteristics> getByFilters(
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
    );
}
