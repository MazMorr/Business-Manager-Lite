package com.marcosoft.storageSoftware.repository;

import com.marcosoft.storageSoftware.model.Configuration;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfigurationRepository extends CrudRepository<Configuration, Long> {
}