package com.marcosoft.storageSoftware.service;

import com.marcosoft.storageSoftware.model.Client;


import java.util.List;

public interface ClientService {

    List<Client> getAllClients();

    Client getClientById(Long id);

    Client saveClient(Client client);

    void deleteClientById(Long id);

}
