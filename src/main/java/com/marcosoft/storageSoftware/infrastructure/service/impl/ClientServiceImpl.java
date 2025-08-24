package com.marcosoft.storageSoftware.infrastructure.service.impl;

import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.repository.ClientRepository;
import com.marcosoft.storageSoftware.domain.service.ClientService;
import com.marcosoft.storageSoftware.infrastructure.util.DisplayAlerts;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final DisplayAlerts displayAlerts;

    public ClientServiceImpl(ClientRepository clientRepository, DisplayAlerts displayAlerts) {
        this.clientRepository = clientRepository;
        this.displayAlerts = displayAlerts;
    }

    @Override
    public Client save(Client client) {
        // Verificar si el cliente ya existe en la base de datos
        boolean isExistingClient = clientRepository.existsById(client.getClientName());

        if (!isExistingClient) {
            // Solo hashear si es un nuevo cliente (no existía antes)
            if (client.getClientPassword() == null || client.getClientPassword().isEmpty()) {
                throw new IllegalArgumentException("La contraseña no puede estar vacía");
            }
            String hashedPassword = BCrypt.hashpw(client.getClientPassword(), BCrypt.gensalt());
            client.setClientPassword(hashedPassword);
        } else {
            // Para clientes existentes, mantener la contraseña actual (ya hasheada)
            // Obtener la contraseña actual de la base de datos y asignarla al objeto
            Client existingClient = clientRepository.findById(client.getClientName()).orElse(null);
            if (existingClient != null) {
                client.setClientPassword(existingClient.getClientPassword());
            }
        }
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
        if (clientName == null || clientPassword == null || clientName.isBlank() || clientPassword.isBlank()) {
            displayAlerts.showError("Nombre de cliente o contraseña no pueden estar vacíos");
            return null;
        }

        Client client = getClientByName(clientName);
        if (client == null) {
            // No mostrar mensaje específico por seguridad
            return null;
        }

        if (!verifyCredentials(clientName, clientPassword)) {
            // No mostrar mensaje específico sobre credenciales incorrectas por seguridad
            return null;
        }

        LocalDateTime lastLogging = client.getLastDateTime();
        LocalDateTime now = LocalDateTime.now();

        if (lastLogging != null && now.isBefore(lastLogging)) {
            String formattedDate = lastLogging.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            displayAlerts.showError("La fecha de su computador es anterior a la última sesión registrada: "
                    + formattedDate + ". Verifique la configuración de fecha y hora.");
            return null;
        }

        // Actualizar la fecha del último inicio de sesión
        client.setLastDateTime(now);

        return client;
    }

    @Override
    public boolean existsByIsClientActive(Boolean isClientActive) {
        return clientRepository.existsByIsClientActive(isClientActive);
    }
}
