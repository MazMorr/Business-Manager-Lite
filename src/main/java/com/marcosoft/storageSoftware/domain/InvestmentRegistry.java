package com.marcosoft.storageSoftware.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Investment_Registry")
public class InvestmentRegistry {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    private Investment investment;

    @Column()
    private String registryType;

    @Column(name="date")
    private LocalDateTime localDateTime;


}
