package com.marcosoft.storageSoftware.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Currency", indexes = {
        @Index(name = "idx_currency_name", columnList = "name"),
        @Index(name = "idx_currency_price", columnList = "currency_price_in_CUP"),
        @Index(name = "idx_currency_name_unique", columnList = "name", unique = true) // Ensure unique currency names
})
public class Currency {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "currency_id")
    private Long id;

    @Column(name = "name")
    private String currencyName;

    @Column(name= "currency_price_in_CUP")
    private Double currencyPriceInCUP;
}
