package com.marcosoft.storageSoftware.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Sell_Registry", indexes = {
        @Index(name = "idx_sellreg_client", columnList = "client_id"),
        @Index(name = "idx_sellreg_date", columnList = "registry_date"),
        @Index(name = "idx_sellreg_sell_date", columnList = "sell_date"),
        @Index(name = "idx_sellreg_product", columnList = "product_name"),
        @Index(name = "idx_sellreg_warehouse", columnList = "warehouse_name"),
})
public class SellRegistry {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "client_id")
    private Client client;

    @Column(name = "registry_type")
    private String registryType;

    @Column(name = "registry_date")
    private LocalDateTime registryDate;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "sell_currency")
    private String sellCurrency;

    @Column(name = "sell_price")
    private Double sellPrice;

    @Column(name = "sell_date")
    private LocalDate sellDate;

    @Column(name = "warehouse_name")
    private String warehouseName;

    @Column(name = "product_amount")
    private Integer productAmount;
}
