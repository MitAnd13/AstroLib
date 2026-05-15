package ru.msu.cmc.cipher.astrolib.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "objects_to_events")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
public class ObjectsToEvents implements CommonEntity<Long>{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, name = "o2e_id")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "o2e_object_id")
    @NonNull
    private AstroObjects object;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "o2e_event_id")
    @NonNull
    private Events event;

    @Column(name = "role")
    @NonNull
    private String role;
}
