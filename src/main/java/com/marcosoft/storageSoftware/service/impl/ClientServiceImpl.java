package com.marcosoft.storageSoftware.service.impl;

import com.marcosoft.storageSoftware.domain.Client;
import com.marcosoft.storageSoftware.repository.ClientRepository;
import com.marcosoft.storageSoftware.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;

    @Autowired
    public ClientServiceImpl(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Override
    public Client save(Client client) {
        return clientRepository.save(client);
    }

    @Override
    public Client getClientById(Long id) {
        return clientRepository.findById(id).orElse(null);
    }

    @Override
    public List<Client> getAllClients() {
        return (List<Client>) clientRepository.findAll();
    }

    @Override
    public void deleteByClientId(Long id) {
        clientRepository.deleteById(id);
    }

    @Override
    public Boolean existsByClientName(String name) {
        return clientRepository.existsByClientName(name);
    }

    @Override
    public Boolean existsByClientNameAndClientPassword(String name, String password) {
        return clientRepository.existsByClientNameAndClientPassword(name, password);
    }

    @Override
    public Client getByIsClientActive(Boolean isActive) {
        return clientRepository.findByIsClientActive(isActive);
    }

    @Override
    public void updateIsClientActiveByClientName(Boolean isActive, String name) {
        clientRepository.updateIsClientActiveByClientName(isActive, name);
    }

    @Override
    public Client getByClientNameAndClientPassword(String clientName, String clientPassword) {
        return clientRepository.findByClientNameAndClientPassword(clientName,clientPassword);
    }

    @Override
    public boolean existsByIsClientActive(Boolean isClientActive) {
        return clientRepository.existsByIsClientActive(isClientActive);
    }
}
