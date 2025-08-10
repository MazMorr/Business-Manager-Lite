package com.marcosoft.storageSoftware.infrastructure.service.impl;

import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.repository.ClientRepository;
import com.marcosoft.storageSoftware.domain.service.ClientService;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;

    public ClientServiceImpl(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Override
    public Client save(Client client) {
        if (client.getClientPassword() == null || client.getClientPassword().isEmpty()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía");
        }

        String hashedPassword = BCrypt.hashpw(client.getClientPassword(), BCrypt.gensalt());
        client.setClientPassword(hashedPassword);
        return clientRepository.save(client);
    }

    @Override
    public boolean verifyCredentials(String clientName, String rawPassword) {
        Client client = clientRepository.findById(clientName).orElse(null);
        if (client == null) {
            return false;
        }
        return BCrypt.checkpw(rawPassword, client.getClientPassword());
    }

    @Override
    public Client getClientByName(String name) {
        return clientRepository.findById(name).orElse(null);
    }

    @Override
    public List<Client> getAllClients() {
        return (List<Client>) clientRepository.findAll();
    }

    @Override
    public void deleteByClientName(String name) {
        clientRepository.deleteById(name);
    }

    @Override
    public Boolean existsByClientName(String name) {
        return clientRepository.existsById(name);
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
    public Client authenticate(String clientName, String clientPassword) {
        Client client = getClientByName(clientName);
        if (client != null && verifyCredentials(clientName, clientPassword)) {
            return client;
        }
        return null;
    }

    @Override
    public boolean existsByIsClientActive(Boolean isClientActive) {
        return clientRepository.existsByIsClientActive(isClientActive);
    }
}
