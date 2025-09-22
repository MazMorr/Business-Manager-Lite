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
@Table(name="Configuration")
public class Configuration {

    @Id
    private Long id;

    @OneToOne
    private Client client;

    @Column(name="logo_business_path", unique = true)
    private String logoBusinessPath;

    @Column(name= "enabled_database_backup", unique = true)
    private Boolean enabledDatabaseBackup;
}
