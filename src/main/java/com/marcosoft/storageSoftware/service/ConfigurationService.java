package com.marcosoft.storageSoftware.service;

import com.marcosoft.storageSoftware.model.Client;
import com.marcosoft.storageSoftware.model.Configuration;

import java.util.List;

public interface ConfigurationService {
    List<Configuration> getAllConfigurations();
    Configuration getConfigurationById(Client clientId);
    Configuration saveConfiguration(Configuration configuration);
    void deleteConfigurationById(Client clientId);
}
