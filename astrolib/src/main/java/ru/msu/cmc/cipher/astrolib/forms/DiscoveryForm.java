package ru.msu.cmc.cipher.astrolib.forms;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class DiscoveryForm {
    private String discoveryKind = "object";

    private String name;
    private String catalogId;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate foundDate;
    private String discoverer;
    private String massMantissa;
    private Integer massExponent;

    private String objectKind;

    private String starSpectre;
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

    private Float rightAscension;
    private Float declension;
    private Long sunDistance;
    private String constellation;

    private Long semiaxis;
    private Float eccentricity;
    private Float inclination;
    private Float longitudeOfAscAngle;
    private Integer minVelocity;
    private Integer maxVelocity;
    private Integer minLight;
    private Integer maxLight;

    private String eventType;
    private String periodicity;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate eventStart;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate eventEnd;
    private List<String> linkedObjectNames = new ArrayList<>();
    private List<String> linkedObjectRoles = new ArrayList<>();

    private String notes;
}
