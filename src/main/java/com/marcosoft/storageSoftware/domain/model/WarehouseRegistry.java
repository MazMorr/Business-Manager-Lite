package com.marcosoft.storageSoftware.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Warehouse_Registry")
public class WarehouseRegistry {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    private Warehouse warehouse;

    @Column(name = "registry_type")
    private String registryType;

    @Column(name = "date")
    private LocalDateTime localDateTime;

    @ManyToOne
    private Client client;
}
