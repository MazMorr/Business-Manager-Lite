package com.marcosoft.storageSoftware.service.impl;

import com.marcosoft.storageSoftware.model.Client;
import com.marcosoft.storageSoftware.model.Configuration;
import com.marcosoft.storageSoftware.repository.ConfigurationRepository;
import com.marcosoft.storageSoftware.service.ConfigurationService;

import java.util.List;

public class ConfigurationServiceImpl implements ConfigurationService {

    private final ConfigurationRepository configurationRepository;

    public ConfigurationServiceImpl(ConfigurationRepository configurationRepository) {
        this.configurationRepository = configurationRepository;
    }

    @Override
    public List<Configuration> getAllConfigurations() {
        return (List<Configuration>) configurationRepository.findAll();
    }

    @Override
    public Configuration getConfigurationById(Client clientId) {
        return configurationRepository.findById(clientId).orElse(null);
    }

    @Override
    public Configuration saveConfiguration(Configuration configuration) {
        return configurationRepository.save(configuration);
    }

    @Override
    public void deleteConfigurationById(Client clientId) {
        configurationRepository.deleteById(clientId);
    }
}
