package com.marcosoft.storageSoftware.infrastructure.service.impl;

import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.model.GeneralRegistry;
import com.marcosoft.storageSoftware.domain.repository.GeneralRegistryRepository;
import com.marcosoft.storageSoftware.domain.service.GeneralRegistryService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

@Lazy
@Service
public class GeneralRegistryServiceImpl implements GeneralRegistryService {
    GeneralRegistryRepository generalRegistryRepository;

    public GeneralRegistryServiceImpl(GeneralRegistryRepository generalRegistryRepository){
        this.generalRegistryRepository = generalRegistryRepository;
    }

    @Override
    public GeneralRegistry save(GeneralRegistry generalRegistry){
        return generalRegistryRepository.save(generalRegistry);
    }

    @Override
    public List<GeneralRegistry> getAllGeneralRegistriesByClient(Client client){
        return generalRegistryRepository.findAllGeneralRegistriesByClient(client);
    }
}
