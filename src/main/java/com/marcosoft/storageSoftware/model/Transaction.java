package com.marcosoft.storageSoftware.model;

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
@Table(name = "Transaction")
public class Transaction implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;

    @Column(name = "transaction_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal transactionPrice;

    @Column(name="category_name", nullable = false)
    private String categoryName;

    @Column(name = "transaction_stock", nullable = false)
    private Integer transactionStock;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(name = "currency_name", nullable = false)
    private String currencyName;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client clientId;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @Column(name= "storage", nullable = false)
    private String transactionStorage;
}
