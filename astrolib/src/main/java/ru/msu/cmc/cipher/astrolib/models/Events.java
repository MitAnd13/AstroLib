package ru.msu.cmc.cipher.astrolib.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "events")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
public class Events implements CommonEntity<Long>{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, name = "eve_id")
    private Long id;

    @Column(nullable = false, unique = true, name = "eve_name")
    @NonNull
    private String name;

    @Column(nullable = false, name = "eve_type")
    @NonNull
    private String type;

    @Column(name = "eve_period")
    private String catalog_id = "уникальное";

    @Column(name = "eve_start")
    private LocalDate start_date = LocalDate.now();

    @Column(name = "eve_end")
    private LocalDate end_date = LocalDate.now();
}
