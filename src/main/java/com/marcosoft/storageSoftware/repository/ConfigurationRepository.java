package com.marcosoft.storageSoftware.repository;

import com.marcosoft.storageSoftware.model.Client;
import com.marcosoft.storageSoftware.model.Configuration;
import org.springframework.data.repository.CrudRepository;

public interface ConfigurationRepository extends CrudRepository<Configuration, Client> {
}