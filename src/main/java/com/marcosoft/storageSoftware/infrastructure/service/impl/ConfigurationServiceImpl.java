package com.marcosoft.storageSoftware.infrastructure.service.impl;

import com.marcosoft.storageSoftware.domain.model.Configuration;
import com.marcosoft.storageSoftware.domain.repository.ConfigurationRepository;
import com.marcosoft.storageSoftware.domain.service.ConfigurationService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

@Lazy
@Service
public class ConfigurationServiceImpl implements ConfigurationService {

    private final ConfigurationRepository configurationRepository;

    @Lazy
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
