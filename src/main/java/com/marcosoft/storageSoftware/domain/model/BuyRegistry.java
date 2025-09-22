package com.marcosoft.storageSoftware.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "Buy_Registry", indexes = {
        @Index(name = "idx_buyreg_datetime", columnList = "date"),
        @Index(name = "idx_buyreg_buy", columnList = "buy_id"),
})
public class BuyRegistry {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "buy_id", nullable = false)
    private Long buyId;

    @Column(name = "buy_name", nullable = false)
    private String buyName;

    @Column(name = "transaction_price", nullable = false)
    private Double buyPrice;

    @Column(name = "currency_name")
    private String currency;

    @ManyToOne
    @JoinColumn(name = "client_id") // This maps the foreign key column
    private Client client;

    @Column(name= "registry_type")
    private String registryType;

    @Column(name="date")
    private LocalDateTime registryDateTime;
}
