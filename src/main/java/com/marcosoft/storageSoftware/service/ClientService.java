package com.marcosoft.storageSoftware.service;

import com.marcosoft.storageSoftware.model.Client;

import java.util.List;

public interface ClientService {

    Client save(Client client);
    Client getClientById(Long id);
    List<Client> getAllClients();
    void deleteByClientId(Long id);
    Boolean existsByClientName(String name);
    Boolean existsByClientNameAndClientPassword(String name, String password);
    Client getByIsClientActive(Boolean isActive);
    void updateIsClientActiveByClientName(Boolean isActive, String name);
    Client getByClientNameAndClientPassword(String clientName, String clientPassword);
    boolean existsByIsClientActive(Boolean isClientActive);
}
