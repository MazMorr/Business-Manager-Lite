package com.marcosoft.storageSoftware.domain.repository;

import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.model.SellRegistry;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface SellRegistryRepository extends CrudRepository<SellRegistry, Long> {

  List<SellRegistry> findAllSellRegistriesByClient(Client client);

}