package com.marcosoft.storageSoftware.domain.service;

import com.marcosoft.storageSoftware.domain.model.Client;

import java.util.List;

public interface ClientService {

    Client save(Client client);

    Client getClientByName(String name);

    List<Client> getAllClients();

    void deleteByClientName(String name);

    Boolean existsByClientName(String name);

    Client authenticate(String clientName, String clientPassword);

    Client getByIsClientActive(Boolean isActive);

    void updateIsClientActiveByClientName(Boolean isActive, String name);

    boolean verifyCredentials(String clientName, String rawPassword);

    boolean existsByIsClientActive(Boolean isClientActive);
}
