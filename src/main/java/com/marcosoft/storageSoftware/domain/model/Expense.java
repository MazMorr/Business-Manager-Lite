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
@Table(name = "Expense")
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long expenseId;

    @Column(name = "expense_name", nullable = false)
    private String expenseName;

    @Column(name = "expense_price", nullable = false)
    private Double expensePrice;

    @ManyToOne
    private Currency currency;

    @Column(name = "amount", nullable = false)
    private Integer amount;

    @Column(name = "is_assigned")
    private Integer leftAmount;

    @Column(name = "received_date", nullable = false)
    private LocalDate receivedDate;

    @Column(name="expense_type")
    private String expenseType;

    @ManyToOne
    private Client client;
}
