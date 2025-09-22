package com.marcosoft.storageSoftware.infrastructure.service.impl;

import com.marcosoft.storageSoftware.domain.model.Buy;
import com.marcosoft.storageSoftware.domain.model.Client;
import com.marcosoft.storageSoftware.domain.model.Currency;
import com.marcosoft.storageSoftware.domain.repository.BuyRepository;
import com.marcosoft.storageSoftware.domain.service.BuyService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class BuyServiceImpl implements BuyService {

    private final BuyRepository buyRepository;

    public BuyServiceImpl(BuyRepository buyRepository) {
        this.buyRepository = buyRepository;
    }

    @Override
    public Buy save(Buy buy) {
        return buyRepository.save(buy);
    }

    @Override
    public List<Buy> getAllBuysGreaterThanZeroByClient(Client client) {
        return buyRepository.findListByLeftAmountGreaterThanAndClient(0, client);
    }

    @Override
    public List<Buy> getAllBuysByClient(Client client) {
        return buyRepository.findListByClient(client);
    }

    @Override
    public void deleteByBuyId(Long buyId) {
        buyRepository.deleteById(buyId);
    }

    @Override
    public Buy getBuyById(Long id) {
        return buyRepository.findById(id).orElse(null);
    }

    @Override
    public Boolean existsByBuyId(Long id) {
        return buyRepository.existsById(id);
    }

    @Override
    public List<Buy> getBuyListByBuyNameAndBuyUnitaryPriceAndBuyTotalPriceAndCurrencyAndAmountAndLeftAmountAndReceivedDateAndBuyTypeAndClientOrderByBuyIdAsc(
            String buyName, Double buyUnitaryPrice, Double buyTotalPrice, Currency currency, Integer amount,
            Integer leftAmount, LocalDate receivedDate, String buyType, Client client) {
        return buyRepository.findListByBuyNameAndBuyUnitaryPriceAndBuyTotalPriceAndCurrencyAndAmountAndLeftAmountAndReceivedDateAndBuyTypeAndClientOrderByBuyIdAsc(
                buyName, buyUnitaryPrice, buyTotalPrice, currency, amount, leftAmount, receivedDate, buyType, client);
    }
}
