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
@Table(name = "Warehouse_Registry", indexes = {
        @Index(name = "idx_warehousereg_client", columnList = "client_id"),
        @Index(name = "idx_warehousereg_datetime", columnList = "date"),
        @Index(name = "idx_warehousereg_warehouse", columnList = "warehouse_name"),
        @Index(name = "idx_warehousereg_product", columnList = "product_name")
})
public class WarehouseRegistry {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "client_id")
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
