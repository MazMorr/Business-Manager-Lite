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
@Table(name = "Sell_Registry")
public class SellRegistry {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    private Client client;

    @Column()
    private String registryType;

    @Column(name = "registry_date")
    private LocalDateTime registryDate;

    @Column(name= "product_name")
    private String productName;

    @Column(name = "sell_currency")
    private String sellCurrency;

    @Column(name= "sell_price")
    private Double sellPrice;

    @Column(name= "sell_date")
    private LocalDate sellDate;

    @Column(name= "warehouse_name")
    private String warehouseName;

    @Column(name = "product_amount")
    private Integer productAmount;

}
