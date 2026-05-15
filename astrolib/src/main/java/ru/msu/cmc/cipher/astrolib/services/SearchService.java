package ru.msu.cmc.cipher.astrolib.services;

import org.springframework.stereotype.Service;
import ru.msu.cmc.cipher.astrolib.dao.AstroObjectDAO;
import ru.msu.cmc.cipher.astrolib.dao.EventDAO;
import ru.msu.cmc.cipher.astrolib.dao.MovingCharacteristicsDAO;
import ru.msu.cmc.cipher.astrolib.dao.StaticCharacteristicsDAO;
import ru.msu.cmc.cipher.astrolib.forms.EventFilterForm;
import ru.msu.cmc.cipher.astrolib.forms.ObjectFilterForm;
import ru.msu.cmc.cipher.astrolib.models.AstroObjects;
import ru.msu.cmc.cipher.astrolib.models.Events;
import ru.msu.cmc.cipher.astrolib.models.MovingCharacteristics;
import ru.msu.cmc.cipher.astrolib.models.ObjectsToEvents;
import ru.msu.cmc.cipher.astrolib.models.StaticCharacteristics;

import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SearchService {
    private static final LocalDate MIN_SEARCH_DATE = LocalDate.of(1, 1, 1);
    private static final LocalDate MAX_SEARCH_DATE = LocalDate.of(9999, 12, 31);

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
        if (ids.isEmpty()) {
            return List.of();
        }

        if (hasCharacteristicFilters(form, type)) {
            ids.retainAll(collectCharacteristicMatches(form, type));
        }

        if (ids.isEmpty()) {
            return List.of();
        }

        return loadObjects(ids);
    }

    public List<Events> searchEventsByFilters(EventFilterForm form) {
        Set<Long> ids = new LinkedHashSet<>(collectEventMatches(form));
        if (ids.isEmpty()) {
            return List.of();
        }

        return ids.stream()
            .map(eventDAO::getById)
            .filter(java.util.Objects::nonNull)
            .sorted((left, right) -> {
                LocalDate leftDate = left.getStart_date();
                LocalDate rightDate = right.getStart_date();
                if (leftDate == null && rightDate == null) {
                    return left.getName().compareToIgnoreCase(right.getName());
                }
                if (leftDate == null) {
                    return 1;
                }
                if (rightDate == null) {
                    return -1;
                }
                int dateCompare = leftDate.compareTo(rightDate);
                return dateCompare != 0 ? dateCompare : left.getName().compareToIgnoreCase(right.getName());
            })
            .toList();
    }

    public AstroObjects getObjectById(Long id) {
        return id == null ? null : astroObjectDAO.getById(id);
    }

    public StaticCharacteristics getStaticCharacteristics(Long objectId) {
        return objectId == null ? null : staticCharacteristicsDAO.getByObjectId(objectId);
    }

    public MovingCharacteristics getMovingCharacteristics(Long objectId) {
        return objectId == null ? null : movingCharacteristicsDAO.getByObjectId(objectId);
    }

    public List<ObjectsToEvents> getEventLinks(Long eventId) {
        return eventId == null ? List.of() : eventDAO.getObjectLinks(eventId).stream().toList();
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

    private Set<Long> collectEventMatches(EventFilterForm form) {
        Set<Long> ids = null;

        String type = normalize(form.getEventType());
        if (type != null) {
            ids = intersect(ids, eventDAO.getByType(type));
        }

        LocalDate startDate = form.getStartDate();
        LocalDate endDate = form.getEndDate();
        if (startDate != null || endDate != null) {
            LocalDate effectiveStart = startDate != null ? startDate : MIN_SEARCH_DATE;
            LocalDate effectiveEnd = endDate != null ? endDate : MAX_SEARCH_DATE;
            ids = intersect(ids, eventDAO.getByDateRange(effectiveStart, effectiveEnd));
        }

        String linkedObjectName = normalize(form.getLinkedObjectName());
        if (linkedObjectName != null) {
            AstroObjects object = astroObjectDAO.getByName(linkedObjectName);
            if (object == null) {
                return Set.of();
            }

            if (startDate != null || endDate != null) {
                LocalDate effectiveStart = startDate != null ? startDate : MIN_SEARCH_DATE;
                LocalDate effectiveEnd = endDate != null ? endDate : MAX_SEARCH_DATE;
                ids = intersect(ids, eventDAO.getByObjectIdAndDateRange(object.getId(), effectiveStart, effectiveEnd));
            } else {
                ids = intersect(ids, eventDAO.getByObjectId(object.getId()));
            }
        }

        String periodicity = normalize(form.getPeriodicity());
        if (periodicity != null) {
            List<Events> baseEvents;
            if (ids == null) {
                baseEvents = eventDAO.getByDateRange(MIN_SEARCH_DATE, MAX_SEARCH_DATE).stream().toList();
            } else {
                baseEvents = ids.stream()
                    .map(eventDAO::getById)
                    .filter(java.util.Objects::nonNull)
                    .toList();
            }

            ids = baseEvents.stream()
                .filter(event -> periodicity.equalsIgnoreCase(event.getCatalog_id()))
                .map(Events::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        }

        if (ids == null) {
            return eventDAO.getByDateRange(MIN_SEARCH_DATE, MAX_SEARCH_DATE).stream()
                .map(Events::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        }

        return ids;
    }

    private Set<Long> intersect(Set<Long> existing, Collection<Events> events) {
        Set<Long> candidate = events.stream()
            .map(Events::getId)
            .collect(Collectors.toCollection(LinkedHashSet::new));

        if (existing == null) {
            return candidate;
        }

        existing.retainAll(candidate);
        return existing;
    }

    private boolean hasCharacteristicFilters(ObjectFilterForm form, AstroObjects.ObjType type) {
        if (isStaticType(type)) {
            return form.getMinRightAscension() != null
                || form.getMaxRightAscension() != null
                || form.getMinDeclension() != null
                || form.getMaxDeclension() != null
                || form.getMinSunDistance() != null
                || form.getMaxSunDistance() != null
                || normalize(form.getConstellation()) != null;
        }

        return form.getMinSemiaxis() != null
            || form.getMaxSemiaxis() != null
            || form.getMinEccentricity() != null
            || form.getMaxEccentricity() != null
            || form.getMinInclination() != null
            || form.getMaxInclination() != null
            || form.getMinLongitudeOfAscAngle() != null
            || form.getMaxLongitudeOfAscAngle() != null
            || form.getMinVelocity() != null
            || form.getMaxVelocity() != null
            || form.getMinLight() != null
            || form.getMaxLight() != null;
    }

    private List<AstroObjects> loadObjects(Set<Long> ids) {
        return ids.stream()
            .map(astroObjectDAO::getById)
            .filter(java.util.Objects::nonNull)
            .sorted((left, right) -> left.getName().compareToIgnoreCase(right.getName()))
            .toList();
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
