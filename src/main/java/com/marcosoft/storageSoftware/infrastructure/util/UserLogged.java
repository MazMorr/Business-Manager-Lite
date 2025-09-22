package com.marcosoft.storageSoftware.infrastructure.util;

import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.infrastructure.service.impl.ClientServiceImpl;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
public class UserLogged {
    private String name;

    private final ClientServiceImpl clientService;

    public UserLogged (ClientServiceImpl clientService){
        this.clientService = clientService;
    }

    public Client getClient() {
        return clientService.getClientByName(name);
    }
}
