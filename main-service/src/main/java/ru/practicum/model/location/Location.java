package ru.practicum.model.location;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "locations")
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Double lat;

    @Column
    private Double lon;

    @Column
    private String name;

    @Column
    private String description;

    @Column
    private String address;

    @Column
    @Enumerated(EnumType.STRING)
    private LocationStatus status;
}
