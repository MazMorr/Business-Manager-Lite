package com.marcosoft.storageSoftware.service;

import com.marcosoft.storageSoftware.domain.Configuration;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ConfigurationService {

    Configuration save(Configuration configuration);

    Configuration getConfigurationById(Long id);

    List<Configuration> getAllConfigurations();

    void deleteById(Long id);

}
