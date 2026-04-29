package ru.msu.cmc.cipher.astrolib.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.msu.cmc.cipher.astrolib.dao.AstroObjectDAO;
import ru.msu.cmc.cipher.astrolib.dao.MovingCharacteristicsDAO;
import ru.msu.cmc.cipher.astrolib.dao.StaticCharacteristicsDAO;
import ru.msu.cmc.cipher.astrolib.forms.DiscoveryForm;
import ru.msu.cmc.cipher.astrolib.models.AstroObjects;
import ru.msu.cmc.cipher.astrolib.models.MovingCharacteristics;
import ru.msu.cmc.cipher.astrolib.models.StaticCharacteristics;

import java.math.BigDecimal;
import java.util.Locale;

@Service
public class DiscoveryService {
    private final AstroObjectDAO astroObjectDAO;
    private final StaticCharacteristicsDAO staticCharacteristicsDAO;
    private final MovingCharacteristicsDAO movingCharacteristicsDAO;

    public DiscoveryService(
        AstroObjectDAO astroObjectDAO,
        StaticCharacteristicsDAO staticCharacteristicsDAO,
        MovingCharacteristicsDAO movingCharacteristicsDAO
    ) {
        this.astroObjectDAO = astroObjectDAO;
        this.staticCharacteristicsDAO = staticCharacteristicsDAO;
        this.movingCharacteristicsDAO = movingCharacteristicsDAO;
    }

    @Transactional
    public void createObject(DiscoveryForm form) {
        validateObjectForm(form);

        AstroObjects object = new AstroObjects();
        object.setName(form.getName().trim());
        object.setType(parseObjectType(form.getObjectKind()));
        object.setCatalog_id(normalize(form.getCatalogId()));
        object.setFound_date(form.getFoundDate());
        object.setFound_name(normalize(form.getDiscoverer()));
        object.setMass(parseMass(form));
        object.setNotes(normalize(form.getNotes()));

        fillTypeSpecificFields(object, form);
        fillParentReferences(object, form);

        astroObjectDAO.insertTyped(object);

        if (isStaticType(object.getType())) {
            staticCharacteristicsDAO.insertForObject(buildStaticCharacteristics(object, form));
        } else {
            movingCharacteristicsDAO.insertForObject(buildMovingCharacteristics(object, form));
        }
    }

    private void validateObjectForm(DiscoveryForm form) {
        if (form == null) {
            throw new IllegalArgumentException("Форма не передана");
        }
        if (!"object".equals(form.getDiscoveryKind())) {
            throw new IllegalArgumentException("Пока поддерживается только добавление объекта");
        }

        require(!isBlank(form.getName()), "Укажите название объекта");
        require(!isBlank(form.getCatalogId()), "Укажите идентификатор каталога");
        require(form.getFoundDate() != null, "Укажите дату открытия");
        require(!isBlank(form.getDiscoverer()), "Укажите первооткрывателя");
        require(!isBlank(form.getObjectKind()), "Выберите класс объекта");
        require(!isBlank(form.getNotes()), "Укажите комментарий к заявке");

        validateMass(form);

        if (astroObjectDAO.existsByName(form.getName().trim())) {
            throw new IllegalArgumentException("Объект с таким названием уже существует");
        }

        AstroObjects.ObjType type = parseObjectType(form.getObjectKind());
        validateTypeSpecificFields(form, type);

        if (isStaticType(type)) {
            require(form.getRightAscension() != null, "Укажите прямое восхождение");
            require(form.getDeclension() != null, "Укажите склонение");
            require(form.getSunDistance() != null, "Укажите расстояние до Солнца");
            require(!isBlank(form.getConstellation()), "Укажите созвездие");
            return;
        }

        require(form.getSemiaxis() != null, "Укажите большую полуось");
        require(form.getEccentricity() != null, "Укажите эксцентриситет орбиты");
        require(form.getInclination() != null, "Укажите наклонение");
        require(form.getLongitudeOfAscAngle() != null, "Укажите долготу восходящего угла");
        require(form.getMinVelocity() != null, "Укажите минимальную скорость");
        require(form.getMaxVelocity() != null, "Укажите максимальную скорость");
        require(form.getMinLight() != null, "Укажите минимальную светимость");
        require(form.getMaxLight() != null, "Укажите максимальную светимость");
    }

    private void validateTypeSpecificFields(DiscoveryForm form, AstroObjects.ObjType type) {
        switch (type) {
            case STAR -> {
                require(!isBlank(form.getStarSpectre()), "Укажите спектральный класс звезды");
                require(!isBlank(form.getStarLight()), "Укажите класс светимости звезды");
                require(form.getStarCount() != null, "Укажите количество компонентов звезды");
            }
            case NEBULA -> require(!isBlank(form.getNebulaType()), "Укажите тип туманности");
            case GALAXY -> require(!isBlank(form.getGalaxyType()), "Укажите тип галактики");
            case PLANET -> {
                require(!isBlank(form.getPlanetType()), "Укажите тип планеты");
                require(!isBlank(form.getPlanetParentStar()), "Укажите родительскую звезду");
            }
            case SATELLITE -> {
                require(!isBlank(form.getSatelliteType()), "Укажите тип спутника");
                require(!isBlank(form.getSatelliteParentPlanet()), "Укажите родительскую планету");
            }
            case ASTEROID -> {
                require(!isBlank(form.getAsteroidSpectre()), "Укажите спектральный класс астероида");
                require(!isBlank(form.getAsteroidGroup()), "Укажите группу астероида");
            }
            case COMET -> {
                require(!isBlank(form.getCometType()), "Укажите тип орбиты кометы");
                require(!isBlank(form.getCometClass()), "Укажите класс кометы");
            }
            case METEOR_SHOWER -> {
                require(!isBlank(form.getMeteorIntensity()), "Укажите интенсивность метеорного потока");
                require(!isBlank(form.getMeteorParentComet()), "Укажите комету-прародителя");
            }
        }
    }

    private void fillTypeSpecificFields(AstroObjects object, DiscoveryForm form) {
        switch (object.getType()) {
            case STAR -> {
                object.setStar_spectre(firstChar(form.getStarSpectre()));
                object.setStar_light(normalize(form.getStarLight()));
                object.setStar_count(form.getStarCount());
            }
            case NEBULA -> object.setNebula_type(normalize(form.getNebulaType()));
            case GALAXY -> object.setGalaxy_type(normalize(form.getGalaxyType()));
            case PLANET -> object.setPlanet_type(normalize(form.getPlanetType()));
            case SATELLITE -> object.setSatellite_type(normalize(form.getSatelliteType()));
            case ASTEROID -> {
                object.setAsteroid_spectre(normalize(form.getAsteroidSpectre()));
                object.setAsteroid_group(normalize(form.getAsteroidGroup()));
            }
            case COMET -> {
                object.setComet_type(normalize(form.getCometType()));
                object.setComet_class(normalize(form.getCometClass()));
            }
            case METEOR_SHOWER -> object.setMeteor_shower_intensity(normalize(form.getMeteorIntensity()));
        }
    }

    private void fillParentReferences(AstroObjects object, DiscoveryForm form) {
        if (object.getType() == AstroObjects.ObjType.PLANET) {
            AstroObjects parent = astroObjectDAO.getByName(form.getPlanetParentStar().trim());
            require(parent != null && parent.getType() == AstroObjects.ObjType.STAR, "Родительская звезда не найдена");
            object.setPlanet_parent_star(parent);
        }
        if (object.getType() == AstroObjects.ObjType.SATELLITE) {
            AstroObjects parent = astroObjectDAO.getByName(form.getSatelliteParentPlanet().trim());
            require(parent != null && parent.getType() == AstroObjects.ObjType.PLANET, "Родительская планета не найдена");
            object.setSatellite_parent_planet(parent);
        }
        if (object.getType() == AstroObjects.ObjType.METEOR_SHOWER) {
            AstroObjects parent = astroObjectDAO.getByName(form.getMeteorParentComet().trim());
            require(parent != null && parent.getType() == AstroObjects.ObjType.COMET, "Комета-прародитель не найдена");
            object.setMeteor_shower_parent(parent);
        }
    }

    private StaticCharacteristics buildStaticCharacteristics(AstroObjects object, DiscoveryForm form) {
        StaticCharacteristics characteristics = new StaticCharacteristics();
        characteristics.setObject(object);
        characteristics.setRight_ascension(form.getRightAscension());
        characteristics.setDeclension(form.getDeclension());
        characteristics.setSun_distance(form.getSunDistance());
        characteristics.setConstellation(form.getConstellation().trim());
        return characteristics;
    }

    private MovingCharacteristics buildMovingCharacteristics(AstroObjects object, DiscoveryForm form) {
        MovingCharacteristics characteristics = new MovingCharacteristics();
        characteristics.setObject(object);
        characteristics.setSemiaxis(form.getSemiaxis());
        characteristics.setEccentricity(form.getEccentricity());
        characteristics.setInclination(form.getInclination());
        characteristics.setLongitude_of_asc_angle(form.getLongitudeOfAscAngle());
        characteristics.setMin_velocity(form.getMinVelocity());
        characteristics.setMax_velocity(form.getMaxVelocity());
        characteristics.setMin_light(form.getMinLight());
        characteristics.setMax_light(form.getMaxLight());
        return characteristics;
    }

    private AstroObjects.ObjType parseObjectType(String objectKind) {
        try {
            return AstroObjects.ObjType.valueOf(objectKind.trim().toUpperCase(Locale.ROOT));
        } catch (Exception exception) {
            throw new IllegalArgumentException("Некорректный класс объекта");
        }
    }

    private Character firstChar(String value) {
        String normalized = normalize(value);
        return normalized == null ? null : normalized.charAt(0);
    }

    private void validateMass(DiscoveryForm form) {
        String mantissa = normalizeMassMantissa(form.getMassMantissa());
        Integer exponent = form.getMassExponent();

        require(mantissa != null, "Укажите мантиссу массы");
        require(exponent != null, "Укажите порядок массы");

        try {
            BigDecimal parsedMantissa = new BigDecimal(mantissa);
            require(parsedMantissa.compareTo(BigDecimal.ZERO) > 0, "Масса должна быть положительной");
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Некорректная мантисса массы");
        }
    }

    private BigDecimal parseMass(DiscoveryForm form) {
        String mantissa = normalizeMassMantissa(form.getMassMantissa());
        Integer exponent = form.getMassExponent();
        return new BigDecimal(mantissa).scaleByPowerOfTen(exponent);
    }

    private boolean isStaticType(AstroObjects.ObjType type) {
        return type == AstroObjects.ObjType.STAR
            || type == AstroObjects.ObjType.NEBULA
            || type == AstroObjects.ObjType.GALAXY;
    }

    private void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String normalize(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private String normalizeMassMantissa(String value) {
        String normalized = normalize(value);
        return normalized == null ? null : normalized.replace(',', '.');
    }
}
