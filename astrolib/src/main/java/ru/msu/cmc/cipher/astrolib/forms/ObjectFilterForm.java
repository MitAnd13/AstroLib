package ru.msu.cmc.cipher.astrolib.forms;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ObjectFilterForm {
    private String objectKind;

    private Character starSpectre;
    private String starLight;
    private Integer starCount;
    private String nebulaType;
    private String galaxyType;
    private String planetType;
    private String satelliteType;
    private String asteroidSpectre;
    private String asteroidGroup;
    private String cometType;
    private String cometClass;
    private String meteorIntensity;

    private String planetParentStar;
    private String satelliteParentPlanet;
    private String meteorParentComet;

    private Float minRightAscension;
    private Float maxRightAscension;
    private Float minDeclension;
    private Float maxDeclension;
    private Long minSunDistance;
    private Long maxSunDistance;
    private String constellation;

    private Long minSemiaxis;
    private Long maxSemiaxis;
    private Float minEccentricity;
    private Float maxEccentricity;
    private Float minInclination;
    private Float maxInclination;
    private Float minLongitudeOfAscAngle;
    private Float maxLongitudeOfAscAngle;
    private Integer minVelocity;
    private Integer maxVelocity;
    private Integer minLight;
    private Integer maxLight;
}
