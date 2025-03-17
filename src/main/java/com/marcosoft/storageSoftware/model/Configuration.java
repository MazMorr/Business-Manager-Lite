package com.marcosoft.storageSoftware.model;

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
    @OneToOne
    @JoinColumn(name = "client_id")
    private Client client;

    @Column(name="dark_mode", unique = true)
    private Boolean darkMode;

    @Column(name="language", unique = true)
    private String language;
}
