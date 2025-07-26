package com.marcosoft.storageSoftware.domain.service;

import com.marcosoft.storageSoftware.domain.model.Configuration;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ConfigurationService {

    Configuration save(Configuration configuration);

    Configuration getConfigurationById(Long id);

    List<Configuration> getAllConfigurations();

    void deleteById(Long id);

}
