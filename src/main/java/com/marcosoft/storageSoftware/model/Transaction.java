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

    //Attributes
    @Id
    @SequenceGenerator(name = "transaction_sequence", sequenceName = "transaction_sequence",
            initialValue = 1, allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "transaction_sequence")
    @Column(name = "transaction_id", nullable = false, unique = true)
    private Long transactionId;

    @Column(name = "transaction_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal transactionPrice;

    @Column(name = "transaction_stock", nullable = false)
    private Integer transactionStock;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(name = "currency_name", nullable = false)
    private String currencyName;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client clientId;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product productId;

    @ManyToOne
    @JoinColumn(name = "tipo_transaccion", nullable = false)
    private TransactionType transactionType;
}
