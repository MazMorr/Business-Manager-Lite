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
        @Index(name = "idx_inventory_client_product", columnList = "client_id, product_id"), // Common access pattern
        @Index(name = "idx_inventory_client_warehouse", columnList = "client_id, warehouse_id"),
        @Index(name = "idx_inventory_amount", columnList = "amount") // For low-stock alerts
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

    @Column(name="amount_alert")
    private Integer amountAlert;

    @Column(name="amount_warning")
    private Integer amountWarning;
}
