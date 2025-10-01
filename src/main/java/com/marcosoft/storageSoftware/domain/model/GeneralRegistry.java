package com.marcosoft.storageSoftware.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "GeneralRegistry")
public class GeneralRegistry {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "client_id")
    private Client client;

    @Column(name = "zone")
    private String affectedZone;

    @Column(name = "registry_type")
    private String registryType;

    @Column(name = "registry_date_time")
    private LocalDateTime registryDateTime;
}
