package ru.msu.cmc.cipher.astrolib.models;

import lombok.*;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "astro_objects")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
public class AstroObjects implements CommonEntity<Long>{
    public enum ObjType {
        STAR,
        NEBULA,
        GALAXY,
        PLANET,
        SATELLITE,
        ASTEROID,
        COMET,
        METEOR_SHOWER
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, name = "obj_id")
    private Long id;

    @Column(nullable = false, unique = true, name = "obj_name")
    @NonNull
    private String name;

    @Column(nullable = false, name = "obj_type")
    @NonNull
    private ObjType type;

    @Column(name = "obj_catalog_id")
    private String catalog_id = "нет данных";

    @Column(name = "obj_found_date")
    private LocalDate found_date = LocalDate.now();

    @Column(name = "obj_found_name")
    private String found_name = "cipher :3";

    @Column(name = "obj_mass")
    private Long mass = Long.valueOf(-1); //undefined

    @Column(name = "star_spectre")
    private Character star_spectre;

    @Column(name = "star_light")
    private String star_light;

    @Column(name = "star_count")
    private Integer star_count;

    @Column(name = "nebula_type")
    private String nebula_type;

    @Column(name = "galaxy_type")
    private String galaxy_type;

    @Column(name = "planet_type")
    private String planet_type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "planet_parent_star")
    private AstroObjects planet_parent_star;

    @Column(name = "satellite_type")
    private String satellite_type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "satellite_parent_planet")
    private AstroObjects satellite_parent_planet;

    @Column(name = "asteroid_spectre")
    private String asteroid_spectre;

    @Column(name = "asteroid_group")
    private String asteroid_group;

    @Column(name = "comet_type")
    private String comet_type;

    @Column(name = "comet_class")
    private String comet_class;

    @Column(name = "meteor_shower_intensity")
    private String meteor_shower_intensity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meteor_shower_parent")
    private AstroObjects meteor_shower_parent;
}
