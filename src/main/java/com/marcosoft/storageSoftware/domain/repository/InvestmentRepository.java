package com.marcosoft.storageSoftware.domain.repository;

import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.model.Currency;
import com.marcosoft.storageSoftware.domain.model.Investment;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
import java.util.List;

public interface InvestmentRepository extends CrudRepository<Investment, Long> {

    boolean existsByInvestmentId(Long investmentId);

    List<Investment> findByLeftAmountGreaterThanAndClient(Integer leftAmount, Client client);

    List<Investment> findAllInvestmentsByClientAndAmountGreaterThanAndInvestmentType(Client client, Integer amount, String investmentType);

    Investment findByClientAndInvestmentNameAndInvestmentPriceAndCurrencyAndAmountAndReceivedDateAndInvestmentType(Client client, String investmentName, Double investmentPrice, Currency currency, Integer amount, LocalDate receivedDate, String investmentType);

    List<Investment> findByClientAndLeftAmountGreaterThanAndInvestmentType(Client client, Integer leftAmount, String investmentType);

}