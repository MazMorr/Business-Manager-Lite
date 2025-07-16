package com.marcosoft.storageSoftware.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Client")
public class Client {

    @Id
    @Column(name = "name", nullable = false, length = 25)
    private String clientName;

    @Column(name = "password", nullable = false)
    private String clientPassword;

    @Column(name="client_company")
    private String clientCompany;

    @Column(name="is_client_active", nullable = false)
    private Boolean isClientActive;
}
