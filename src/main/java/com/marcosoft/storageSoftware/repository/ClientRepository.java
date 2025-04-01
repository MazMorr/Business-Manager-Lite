package com.marcosoft.storageSoftware.repository;

import com.marcosoft.storageSoftware.model.Client;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ClientRepository extends CrudRepository<Client, Long> {

    Client findByClientNameAndClientPassword(String clientName, String clientPassword);

    boolean existsByClientNameAndClientPassword(String clientName, String clientPassword);

    Client findByIsClientActive(Boolean isClientActive);

    boolean existsByClientName(String clientName);

    @Transactional
    @Modifying
    @Query("update Client c set c.isClientActive = ?1 where c.clientName = ?2")
    int updateIsClientActiveByClientName(Boolean isClientActive, String clientName);

    boolean existsByIsClientActive(Boolean isClientActive);


}