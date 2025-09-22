package com.marcosoft.storageSoftware.domain.service;

import com.marcosoft.storageSoftware.domain.model.Buy;
import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.model.Currency;

import java.time.LocalDate;
import java.util.List;

public interface BuyService {
    Buy save(Buy buy);

    List<Buy> getAllBuysGreaterThanZeroByClient(Client client);

    List<Buy> getAllBuysByClient(Client client);

    void deleteByBuyId(Long buyId);

    Buy getBuyById(Long id);

    Boolean existsByBuyId(Long id);

    List<Buy> getBuyListByBuyNameAndBuyUnitaryPriceAndBuyTotalPriceAndCurrencyAndAmountAndLeftAmountAndReceivedDateAndBuyTypeAndClientOrderByBuyIdAsc(
            String buyName, Double buyUnitaryPrice, Double buyTotalPrice, Currency currency, Integer amount,
            Integer leftAmount, LocalDate receivedDate, String buyType, Client client);
}
