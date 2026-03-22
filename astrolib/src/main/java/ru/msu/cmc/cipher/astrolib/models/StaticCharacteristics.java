package ru.msu.cmc.cipher.astrolib.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "static_characteristics")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
public class StaticCharacteristics implements CommonEntity<Long>{
    @Id
    private Long id;

    @OneToOne()
    @MapsId
    private AstroObjects object;

    @Column(nullable = false, name = "sc_right_ascension")
    @NonNull
    private Float right_ascension;

    @Column(nullable = false, name = "sc_declension")
    @NonNull
    private Float declension;

    @Column(nullable = false, name = "sc_sun_distance")
    @NonNull
    private Long sun_distance;

    @Column(nullable = false, name = "sc_constellation")
    @NonNull
    private String constellation;
}
