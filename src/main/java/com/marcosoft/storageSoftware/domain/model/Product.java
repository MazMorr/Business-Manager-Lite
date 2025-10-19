package com.marcosoft.storageSoftware.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author MazMorr
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Product", indexes = {
        @Index(name = "idx_product_client", columnList = "client_id"),
        @Index(name = "idx_product_name", columnList = "product_name")
})
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "product_name", nullable = false, length = 20, unique = true)
    private String productName;

    @Column(name = "sell_price")
    private Double sellPrice;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "client_id")
    private Client client;

    @ManyToOne
    private Currency currency;
}
