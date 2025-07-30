package com.marcosoft.storageSoftware.domain.service;

import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.model.GeneralRegistry;

import java.util.List;

public interface GeneralRegistryService {
    GeneralRegistry save(GeneralRegistry generalRegistry);
    List<GeneralRegistry> getAllGeneralRegistriesByClient(Client client);
}
