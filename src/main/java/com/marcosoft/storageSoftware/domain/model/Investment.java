package com.marcosoft.storageSoftware.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Investment")
public class Investment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long investmentId;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "transaction_price", nullable = false)
    private Double investmentPrice;

    @ManyToOne
    private Currency currency;

    @Column(name = "amount", nullable = false)
    private Integer amount;

    @Column(name = "is_assigned")
    private Integer leftAmount;

    @Column(name = "received_date", nullable = false)
    private LocalDate receivedDate;

    @Column(name="investment_type")
    private String investmentType;

    @ManyToOne
    private Client client;



}
