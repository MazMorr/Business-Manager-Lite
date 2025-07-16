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
@Table(name="Configuration")
public class Configuration {

    @Id
    private Long id;

    @OneToOne
    private Client client;

    @Column(name="window_mode", unique = true)
    private Boolean windowMode;

    @Column(name="dark_mode", unique = true)
    private Boolean darkMode;

    @Column(name="language", unique = true)
    private String language;
}
