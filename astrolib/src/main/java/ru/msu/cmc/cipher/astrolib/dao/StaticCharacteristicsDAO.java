package ru.msu.cmc.cipher.astrolib.dao;

import ru.msu.cmc.cipher.astrolib.models.AstroObjects;
import ru.msu.cmc.cipher.astrolib.models.StaticCharacteristics;

import java.util.Collection;

public interface StaticCharacteristicsDAO extends CommonDAO<StaticCharacteristics, Long> {

    void insertForObject(StaticCharacteristics characteristics);
    void updateForObject(StaticCharacteristics characteristics);

    StaticCharacteristics getByObjectId(Long objectId);
    Collection<StaticCharacteristics> getAllWithObjects();
    Collection<StaticCharacteristics> getByObjectType(AstroObjects.ObjType type);
    Collection<StaticCharacteristics> getByFilters(
        AstroObjects.ObjType type,
        Float minRightAscension,
        Float maxRightAscension,
        Float minDeclension,
        Float maxDeclension,
        Long minSunDistance,
        Long maxSunDistance,
        String constellation
    );
}
