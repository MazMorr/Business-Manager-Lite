package com.marcosoft.storageSoftware.service;

import com.marcosoft.storageSoftware.domain.Client;

import java.util.List;

public interface ClientService {

    Client save(Client client);

    Client getClientByName(String name);

    List<Client> getAllClients();

    void deleteByClientName(String name);

    Boolean existsByClientName(String name);

    Boolean existsByClientNameAndClientPassword(String name, String password);

    Client getByIsClientActive(Boolean isActive);

    void updateIsClientActiveByClientName(Boolean isActive, String name);

    Client getByClientNameAndClientPassword(String clientName, String clientPassword);

    boolean existsByIsClientActive(Boolean isClientActive);
}
