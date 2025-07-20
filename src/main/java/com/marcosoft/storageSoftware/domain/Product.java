package com.marcosoft.storageSoftware.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * @author MazMorr
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Product")
public class Product {

    @Id
    @Column(name = "product_name", nullable = false, length = 20)
    private String productName;

    @Column(name = "sell_price")
    private Double sellPrice;

    @ManyToOne
    private Client client;
}
