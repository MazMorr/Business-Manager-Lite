package com.marcosoft.storageSoftware.domain.repository;

import com.marcosoft.storageSoftware.domain.model.Buy;
import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.model.Currency;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BuyRepository extends CrudRepository<Buy, Long> {
    List<Buy> findListByLeftAmountGreaterThanAndClient(Integer leftAmount, Client client);

    List<Buy> findListByClient(Client client);

    List<Buy> findListByBuyNameAndBuyUnitaryPriceAndBuyTotalPriceAndCurrencyAndAmountAndLeftAmountAndReceivedDateAndBuyTypeAndClientOrderByBuyIdAsc(
            String buyName, Double buyUnitaryPrice, Double buyTotalPrice, Currency currency, Integer amount,
            Integer leftAmount, LocalDate receivedDate, String buyType, Client client);

    List<Buy> findByBuyNameAndClient(String buyName, Client client);
}