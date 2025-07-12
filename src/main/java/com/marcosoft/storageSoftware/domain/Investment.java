package com.marcosoft.storageSoftware.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
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

    @Column(name = "transaction_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal investmentPrice;

    @ManyToOne
    private Currency currency;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "amount", nullable = false)
    private Integer amount;

    @Column(name = "received_date", nullable = false)
    private LocalDate receivedDate;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

}
