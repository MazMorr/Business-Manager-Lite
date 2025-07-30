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

    @Column(name = "registry_type")
    private String registryType;

    @Column(name = "date")
    private LocalDateTime registryDateTime;

    @ManyToOne
    private Warehouse warehouse;

    @ManyToOne
    private Product product;

    @ManyToOne
    private Client client;

    @Column(name = "amount")
    private Integer amount;

}
