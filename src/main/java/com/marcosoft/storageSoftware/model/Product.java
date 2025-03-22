
package com.marcosoft.storageSoftware.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author MazMorr
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Producto")
public class Product implements Serializable {

    //Atributes
    @Id
    @SequenceGenerator(name = "product_sequence", sequenceName = "product_sequence",
            initialValue = 1, allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_sequence")
    @Column(name = "product_id", nullable = false, unique = true)
    private Long productId;

    @Column(nullable = false, length = 20)
    private String productName;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category idCategory;

    @Column(name="quantity", nullable = false)
    private Integer quantityInStorage;
}
