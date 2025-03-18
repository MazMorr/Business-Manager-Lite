package com.marcosoft.storageSoftware.service.impl;

import com.marcosoft.storageSoftware.model.Wallet;
import com.marcosoft.storageSoftware.repository.WalletRepository;
import com.marcosoft.storageSoftware.service.WalletService;

import java.util.List;

public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;

    public WalletServiceImpl(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @Override
    public List<Wallet> getAllWallets() {
        return (List<Wallet>) walletRepository.findAll();
    }

    @Override
    public Wallet getWalletById(Long id) {
        return walletRepository.findById(id).orElse(null);
    }

    @Override
    public Wallet saveWallet(Wallet wallet) {
        return walletRepository.save(wallet);
    }

    @Override
    public void deleteWalletById(Long id) {
        walletRepository.deleteById(id);
    }
}
