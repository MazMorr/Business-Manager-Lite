package com.marcosoft.storageSoftware.service.impl;

import com.marcosoft.storageSoftware.model.Configuration;
import com.marcosoft.storageSoftware.repository.ConfigurationRepository;
import com.marcosoft.storageSoftware.service.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConfigurationServiceImpl implements ConfigurationService {

    private final ConfigurationRepository configurationRepository;

    @Autowired
    public ConfigurationServiceImpl(ConfigurationRepository configurationRepository){
        this.configurationRepository=configurationRepository;
    }

    @Override
    public Configuration save(Configuration configuration) {
        return configurationRepository.save(configuration);
    }

    @Override
    public Configuration getConfigurationById(Long id) {
        return configurationRepository.findById(id).orElse(null);
    }

    @Override
    public List<Configuration> getAllConfigurations() {
        return (List<Configuration>) configurationRepository.findAll();
    }

    @Override
    public void deleteById(Long id) {
        configurationRepository.deleteById(id);
    }
}
