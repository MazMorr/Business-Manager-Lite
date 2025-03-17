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
@Table(name = "Category")
public class Category implements Serializable {

    // Attributes
    @Id
    @SequenceGenerator(name = "category_sequence", sequenceName = "category_sequence",
            initialValue = 1, allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "category_sequence")
    @Column(name = "id_category", nullable = false, unique = true)
    private Long categoryId;

    @Column(name = "category_name", nullable = false, length = 20)
    private String categoryName;

}
