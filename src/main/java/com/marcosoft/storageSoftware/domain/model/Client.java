package com.marcosoft.storageSoftware.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Clients", indexes = {
        @Index(name = "idx_clients_last_datetime", columnList = "last_date_time"),
})
public class Client {

    @Id
    @Column(name = "name", nullable = false, length = 25)
    private String clientName;

    @Column(name = "password", nullable = false)
    private String clientPassword;

    @Column(name="client_company")
    private String clientCompany;

    @Column(name = "last_date_time")
    private LocalDateTime lastDateTime;
}
