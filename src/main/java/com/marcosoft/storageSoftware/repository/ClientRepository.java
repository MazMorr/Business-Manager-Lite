package com.marcosoft.storageSoftware.repository;

import com.marcosoft.storageSoftware.model.Client;
import org.springframework.data.repository.CrudRepository;

public interface ClientRepository extends CrudRepository<Client, Long> {

    Client findByClientNameAndClientPassword(String clientName, String clientPassword);

    static boolean existsByClientNameAndClientPassword(String clientName, String clientPassword) {
        return false;
    }


}