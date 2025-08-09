package com.marcosoft.storageSoftware.infrastructure.security;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Clase que representa los datos de la licencia
 */
@Getter
@Setter
public class LicenseData {

    private String clientName;
    private LocalDate expirationDate;
    private boolean isUsed;
    private String licenseId;

    public LicenseData() {
        this.licenseId = UUID.randomUUID().toString();
        this.isUsed = false;
    }

    @Override
    public String toString() {
        return "LicenseData{" +
                "clientName='" + clientName + '\'' +
                ", expirationDate=" + expirationDate +
                ", isUsed=" + isUsed +
                ", licenseId='" + licenseId + '\'' +
                '}';
    }
}