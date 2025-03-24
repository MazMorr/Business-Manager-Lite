package com.marcosoft.storageSoftware.service;

import com.marcosoft.storageSoftware.model.Wallet;

import java.util.List;

public interface WalletService {
    Wallet save(Wallet wallet);
    Wallet getWalletById(Long id);
    List<Wallet> getAllWallets();
    void deleteById(Long id);
}
