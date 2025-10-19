package com.marcosoft.storageSoftware.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;


//Esto nueva base de datos hay que ponerla en el script de migración
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "Buy", indexes = {
        @Index(name = "idx_buy_client", columnList = "client_id"),
        @Index(name = "idx_buy_received_date", columnList = "received_Date"),
        @Index(name = "idx_buy_expense_type", columnList = "buy_type"),
})
public class Buy {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "buy_seq")
    @SequenceGenerator(name = "buy_seq", sequenceName = "buy_sequence", allocationSize = 1)
    private Long buyId;

    @Column(name = "buy_name", nullable = false)
    private String buyName;

    @Column(name = "buy_unit_price", nullable = false)
    private Double buyUnitaryPrice;

    @Column(name = "buy_total_price", nullable = false)
    private Double buyTotalPrice;

    @ManyToOne
    private Currency currency;

    @Column(name = "amount", nullable = false)
    private Integer amount;

    @Column(name = "left_amount")
    private Integer leftAmount;

    @Column(name = "received_date", nullable = false)
    private LocalDate receivedDate;

    @Column(name = "buy_type")
    private String buyType;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "client_id")
    private Client client;
}
