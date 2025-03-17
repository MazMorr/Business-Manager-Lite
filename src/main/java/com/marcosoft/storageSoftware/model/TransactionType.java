/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.marcosoft.storageSoftware.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author MazMorr
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "Transaction_Type")
public class TransactionType implements Serializable {

    //Attributes
    @Id
    @SequenceGenerator(name = "transaction_type_sequence", sequenceName = "transaction_type_sequence",
            initialValue = 1, allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "transaction_type_sequence")
    @Column(name = "id_transaction_type", nullable = false, unique = true)
    private Long idTransactionType;

    @Column(name = "transaction_name", nullable = false, length = 15)
    private String transactionName;
}
