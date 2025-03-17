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

    //****** Faltan los sequence generator *******
    //Attributes
    @Id
    @SequenceGenerator(name = "transaction_sequence", sequenceName = "transaction_sequence",
            initialValue = 1, allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "transaction_sequence")
    @Column(name = "id_transaction", nullable = false, unique = true)
    private Long transactionId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stock;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne
    @JoinColumn(name = "id_moneda", nullable = false)
    private Currency currency;

    @ManyToOne
    @JoinColumn(name = "id_client", nullable = false)
    private Client idClient;

    @ManyToOne
    @JoinColumn(name = "id_product", nullable = false)
    private Product idProduct;

    @ManyToOne
    @JoinColumn(name = "tipo_transaccion", nullable = false)
    private TransactionType transactionType;

}
