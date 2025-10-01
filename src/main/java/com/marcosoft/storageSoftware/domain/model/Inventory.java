package com.marcosoft.storageSoftware.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Inventory", indexes = {
        @Index(name = "idx_inventory_amount", columnList = "amount")
})
public class Inventory {
    @Id
    @Column(name = "id_inventory")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "client_id")
    private Client client;

    @ManyToOne
    private Warehouse warehouse;

    @Column(name = "amount")
    private Integer amount;

    @Column(name = "unit_price") // NUEVO CAMPO - precio unitario al momento de la compra
    private Double unitPrice;

    @Column(name = "currency") // NUEVO CAMPO - moneda del precio
    private String currency;

    @Column(name = "buy_id") // NUEVO CAMPO - referencia a la compra original
    private Long buyId;

    @Column(name="amount_alert")
    private Integer amountAlert;

    @Column(name="amount_warning")
    private Integer amountWarning;
}