package com.marcosoft.storageSoftware.infrastructure.util;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Lazy
@Component
public class ParseDataTypes {
    public Long parseLong(String value) {
        try {
            return (value == null || value.isEmpty()) ? null : Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Double parseDouble(String value) {
        try {
            return (value == null || value.isEmpty()) ? null : Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Integer parseInt(String value) {
        try {
            return (value == null || value.isEmpty()) ? null : Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
