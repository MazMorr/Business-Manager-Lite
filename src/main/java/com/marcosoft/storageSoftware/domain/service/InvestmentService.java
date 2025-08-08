package com.marcosoft.storageSoftware.domain.service;

import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.model.Currency;
import com.marcosoft.storageSoftware.domain.model.Investment;

import java.time.LocalDate;
import java.util.List;

public interface InvestmentService {
    Investment save(Investment investment);

    Investment getInvestmentById(Long id);

    List<Investment> getAllInvestments();

    void deleteInvestmentById(Long id);

    boolean existsByInvestmentId(Long investmentId);

    Investment getByClientAndInvestmentNameAndInvestmentPriceAndCurrencyAndAmountAndReceivedDateAndInvestmentType(Client client, String investmentName, Double investmentPrice, Currency currency, Integer amount, LocalDate receivedDate, String investmentType);

    List<Investment> getAllInvestmentsByClientAndAmountGreaterThanZeroAndInvestmentType(Client client, String investmentType);

    List<Investment> getAllProductInvestmentsGreaterThanZeroByClient(Client client);

    List<Investment> getAllInvestmentsByLeftAmountGreaterThanAndClient(Integer leftAmount, Client client);
}
