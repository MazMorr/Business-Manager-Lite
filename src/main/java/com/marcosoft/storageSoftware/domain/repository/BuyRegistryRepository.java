package com.marcosoft.storageSoftware.domain.repository;

import com.marcosoft.storageSoftware.domain.model.BuyRegistry;
import com.marcosoft.storageSoftware.domain.model.Client;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface BuyRegistryRepository extends CrudRepository<BuyRegistry, Long> {

    List<BuyRegistry> findListByClient(Client client);
}