package com.marcosoft.storageSoftware.domain.repository;

import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.model.GeneralRegistry;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface GeneralRegistryRepository extends CrudRepository<GeneralRegistry, Long> {
    List<GeneralRegistry> findAllGeneralRegistriesByClient(Client client);
}