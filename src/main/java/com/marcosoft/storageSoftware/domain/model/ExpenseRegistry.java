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
@Table(name = "Investment_Registry")
public class ExpenseRegistry {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "investment_id", nullable = false)
    private Long investmentId;

    @Column(name = "investment_name", nullable = false)
    private String investmentName;

    @Column(name = "transaction_price", nullable = false)
    private Double investmentPrice;

    @Column(name = "currency_name")
    private String currency;

    @ManyToOne
    private Client client;

    @Column(name= "registry_type")
    private String registryType;

    @Column(name="date")
    private LocalDateTime registryDateTime;
}
