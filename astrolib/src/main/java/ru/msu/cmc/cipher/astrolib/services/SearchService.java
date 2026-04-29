package ru.msu.cmc.cipher.astrolib.services;

import org.springframework.stereotype.Service;
import ru.msu.cmc.cipher.astrolib.dao.AstroObjectDAO;
import ru.msu.cmc.cipher.astrolib.dao.EventDAO;
import ru.msu.cmc.cipher.astrolib.dao.MovingCharacteristicsDAO;
import ru.msu.cmc.cipher.astrolib.dao.StaticCharacteristicsDAO;
import ru.msu.cmc.cipher.astrolib.forms.ObjectFilterForm;
import ru.msu.cmc.cipher.astrolib.models.AstroObjects;
import ru.msu.cmc.cipher.astrolib.models.Events;
import ru.msu.cmc.cipher.astrolib.models.MovingCharacteristics;
import ru.msu.cmc.cipher.astrolib.models.StaticCharacteristics;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SearchService {
    private final AstroObjectDAO astroObjectDAO;
    private final EventDAO eventDAO;
    private final StaticCharacteristicsDAO staticCharacteristicsDAO;
    private final MovingCharacteristicsDAO movingCharacteristicsDAO;

    public SearchService(
        AstroObjectDAO astroObjectDAO,
        EventDAO eventDAO,
        StaticCharacteristicsDAO staticCharacteristicsDAO,
        MovingCharacteristicsDAO movingCharacteristicsDAO
    ) {
        this.astroObjectDAO = astroObjectDAO;
        this.eventDAO = eventDAO;
        this.staticCharacteristicsDAO = staticCharacteristicsDAO;
        this.movingCharacteristicsDAO = movingCharacteristicsDAO;
    }

    public List<AstroObjects> searchObjectsByName(String query) {
        String normalized = normalize(query);
        if (normalized == null) {
            return List.of();
        }
        return astroObjectDAO.getByNameLike(normalized).stream().toList();
    }

    public List<Events> searchEventsByName(String query) {
        String normalized = normalize(query);
        if (normalized == null) {
            return List.of();
        }
        return eventDAO.getByNameLike(normalized).stream().toList();
    }

    public List<AstroObjects> searchObjectsByFilters(ObjectFilterForm form) {
        AstroObjects.ObjType type = parseType(form.getObjectKind());
        if (type == null) {
            return List.of();
        }

        Set<Long> ids = new LinkedHashSet<>(collectBaseMatches(form, type));
        ids.retainAll(collectCharacteristicMatches(form, type));

        if (ids.isEmpty()) {
            return List.of();
        }

        return ids.stream()
            .map(astroObjectDAO::getById)
            .filter(java.util.Objects::nonNull)
            .sorted((left, right) -> left.getName().compareToIgnoreCase(right.getName()))
            .toList();
    }

    private Set<Long> collectBaseMatches(ObjectFilterForm form, AstroObjects.ObjType type) {
        Collection<AstroObjects> matches = switch (type) {
            case STAR -> astroObjectDAO.getStarsByFilters(form.getStarSpectre(), normalize(form.getStarLight()), form.getStarCount());
            case NEBULA -> astroObjectDAO.getNebulaeByFilters(normalize(form.getNebulaType()));
            case GALAXY -> astroObjectDAO.getGalaxiesByFilters(normalize(form.getGalaxyType()));
            case PLANET -> astroObjectDAO.getPlanetsByFilters(normalize(form.getPlanetType()), resolveParentId(form.getPlanetParentStar(), AstroObjects.ObjType.STAR));
            case SATELLITE -> astroObjectDAO.getSatellitesByFilters(normalize(form.getSatelliteType()), resolveParentId(form.getSatelliteParentPlanet(), AstroObjects.ObjType.PLANET));
            case ASTEROID -> astroObjectDAO.getAsteroidsByFilters(normalize(form.getAsteroidSpectre()), normalize(form.getAsteroidGroup()));
            case COMET -> astroObjectDAO.getCometsByFilters(normalize(form.getCometType()), normalize(form.getCometClass()));
            case METEOR_SHOWER -> astroObjectDAO.getMeteorShowersByFilters(normalize(form.getMeteorIntensity()), resolveParentId(form.getMeteorParentComet(), AstroObjects.ObjType.COMET));
        };

        return matches.stream()
            .map(AstroObjects::getId)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Set<Long> collectCharacteristicMatches(ObjectFilterForm form, AstroObjects.ObjType type) {
        if (isStaticType(type)) {
            return staticCharacteristicsDAO.getByFilters(
                    type,
                    form.getMinRightAscension(),
                    form.getMaxRightAscension(),
                    form.getMinDeclension(),
                    form.getMaxDeclension(),
                    form.getMinSunDistance(),
                    form.getMaxSunDistance(),
                    normalize(form.getConstellation())
                ).stream()
                .map(StaticCharacteristics::getObject)
                .map(AstroObjects::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        }

        return movingCharacteristicsDAO.getByFilters(
                type,
                form.getMinSemiaxis(),
                form.getMaxSemiaxis(),
                form.getMinEccentricity(),
                form.getMaxEccentricity(),
                form.getMinInclination(),
                form.getMaxInclination(),
                form.getMinLongitudeOfAscAngle(),
                form.getMaxLongitudeOfAscAngle(),
                form.getMinVelocity(),
                form.getMaxVelocity(),
                form.getMinLight(),
                form.getMaxLight()
            ).stream()
            .map(MovingCharacteristics::getObject)
            .map(AstroObjects::getId)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Long resolveParentId(String name, AstroObjects.ObjType expectedType) {
        String normalized = normalize(name);
        if (normalized == null) {
            return null;
        }

        AstroObjects parent = astroObjectDAO.getByName(normalized);
        if (parent == null || parent.getType() != expectedType) {
            return Long.MIN_VALUE;
        }
        return parent.getId();
    }

    private AstroObjects.ObjType parseType(String objectKind) {
        String normalized = normalize(objectKind);
        if (normalized == null) {
            return null;
        }

        try {
            return AstroObjects.ObjType.valueOf(normalized.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private boolean isStaticType(AstroObjects.ObjType type) {
        return type == AstroObjects.ObjType.STAR
            || type == AstroObjects.ObjType.NEBULA
            || type == AstroObjects.ObjType.GALAXY;
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
