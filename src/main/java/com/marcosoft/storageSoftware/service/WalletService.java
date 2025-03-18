package com.marcosoft.storageSoftware.service;

import com.marcosoft.storageSoftware.model.Wallet;

import java.util.List;

public interface WalletService {
    List<Wallet> getAllWallets();
    Wallet getWalletById(Long id);
    Wallet saveWallet(Wallet wallet);
    void deleteWalletById(Long id);
}
