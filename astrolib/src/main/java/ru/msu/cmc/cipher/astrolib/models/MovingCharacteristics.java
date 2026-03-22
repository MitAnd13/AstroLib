package ru.msu.cmc.cipher.astrolib.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "moving_characteristics")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
public class MovingCharacteristics implements CommonEntity<Long>{
    @Id
    private Long id;

    @OneToOne()
    @MapsId
    private AstroObjects object;

    @Column(nullable = false, name = "mc_semiaxis")
    @NonNull
    private Long semiaxis;

    @Column(nullable = false, name = "mc_eccentricity")
    @NonNull
    private Float eccentricity;

    @Column(nullable = false, name = "mc_inclination")
    @NonNull
    private Float inclination;

    @Column(nullable = false, name = "mc_longitude_of_asc_angle")
    @NonNull
    private Float longitude_of_asc_angle;

    @Column(nullable = false, name = "mc_min_velocity")
    @NonNull
    private Integer min_velocity;

    @Column(nullable = false, name = "mc_max_velocity")
    @NonNull
    private Integer max_velocity;

    @Column(nullable = false, name = "mc_min_light")
    @NonNull
    private Integer min_light;

    @Column(nullable = false, name = "mc_max_light")
    @NonNull
    private Integer max_light;
}
