package com.marcosoft.storageSoftware.infrastructure.util;

import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class BuyAndExpenseSharedMethods {
    public Double parsePriceFromString(String priceText) {
        if (priceText == null || priceText.isEmpty()) {
            return null;
        }

        // Usar regex para encontrar el primer número (entero o decimal)
        Matcher matcher = Pattern.compile("\\d+(?:\\.\\d+)?").matcher(priceText.trim());
        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group());
            } catch (NumberFormatException e) {
                return null;
            }
        }

        return null;
    }
}
