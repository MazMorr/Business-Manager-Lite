package com.marcosoft.storageSoftware.model;

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
    @SequenceGenerator(name = "client_sequence", sequenceName = "client_sequence",
            initialValue = 1, allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "client_sequence")
    @Column(name = "client_id", nullable = false, unique = true)
    private Long clientId;

    @Column(name = "name", nullable = false, length = 25)
    private String clientName;

    @Column(name = "password", nullable = false)
    private String clientPassword;
}
