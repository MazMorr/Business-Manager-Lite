package com.marcosoft.storageSoftware.domain;

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
public class Warehouse {

    @Id
    @Column(name="warehouse_name")
    private String warehouseName;

    @ManyToOne
    private Client client;
}
