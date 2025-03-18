package com.marcosoft.storageSoftware.service.impl;

import com.marcosoft.storageSoftware.model.Client;
import com.marcosoft.storageSoftware.repository.ClientRepository;
import com.marcosoft.storageSoftware.service.ClientService;

import java.util.List;

public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;

    public ClientServiceImpl(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Override
    public List<Client> getAllClients() {
        return (List<Client>) clientRepository.findAll();
    }

    @Override
    public Client getClientById(Long id) {
        return clientRepository.findById(id).orElse(null);
    }

    @Override
    public Client saveClient(Client client) {
        return clientRepository.save(client);
    }

    @Override
    public void deleteClientById(Long id) {
        clientRepository.deleteById(id);
    }
}
