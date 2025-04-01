package com.marcosoft.storageSoftware.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * @author MazMorr
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Product")
public class Product implements Serializable {

    //Atributes
    @Id
    @Column(nullable = false, length = 20)
    private String productName;

    @Column(name = "category", nullable = false)
    private String categoryName;

    @Column(name="quantity", nullable = false)
    private Integer quantityInStorage;

    @Column(name= "price_per_unit", nullable = false, scale = 2, precision = 10)
    private BigDecimal pricePerUnit;

    @Column(name="currency", nullable = false)
    private String currencyName;

    @Column(name="stored_in", nullable = false)
    private String storedIn;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(productName, product.productName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productName);
    }
}
