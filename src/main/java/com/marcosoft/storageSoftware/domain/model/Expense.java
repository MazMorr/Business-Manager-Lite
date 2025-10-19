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
@Table(name = "Expense", indexes = {
        @Index(name = "idx_expense_client", columnList = "client_id"),
        @Index(name = "idx_expense_received_date", columnList = "received_date"),
        @Index(name = "idx_expense_type", columnList = "expense_type"),
})
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "expense_seq")
    @SequenceGenerator(name = "expense_seq", sequenceName = "expense_seq", allocationSize = 1)
    private Long expenseId;

    @Column(name = "expense_name", nullable = false)
    private String expenseName;

    @Column(name = "expense_price", nullable = false)
    private Double expensePrice;

    @ManyToOne
    private Currency currency;

    @Column(name = "amount")
    private Integer amount;

    @Column(name = "received_date", nullable = false)
    private LocalDate receivedDate;

    @Column(name="expense_type")
    private String expenseType;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "client_id")
    private Client client;
}
