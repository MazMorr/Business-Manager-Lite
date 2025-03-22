package com.marcosoft.storageSoftware.repository;

import com.marcosoft.storageSoftware.model.Client;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository extends CrudRepository<Client, Long> {

    Client findByClientNameAndClientPassword(String clientName, String clientPassword);

    boolean existsByClientNameAndClientPassword(String clientName, String clientPassword);

    Client findByIsClientActive(Boolean isClientActive);

    boolean existsByClientName(String clientName);

}