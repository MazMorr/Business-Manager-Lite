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
    private Client client;

    @Column(name = "registry_type")
    private String registryType;

    @Column(name = "date")
    private LocalDateTime registryDateTime;

    @Column(name = "warehouse_name")
    private String warehouseName;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "amount")
    private Integer amount;
}
