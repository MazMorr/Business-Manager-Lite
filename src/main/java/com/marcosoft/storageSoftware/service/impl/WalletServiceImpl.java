package com.marcosoft.storageSoftware.service.impl;

import com.marcosoft.storageSoftware.model.Wallet;
import com.marcosoft.storageSoftware.repository.WalletRepository;
import com.marcosoft.storageSoftware.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;

    @Autowired
    public WalletServiceImpl(WalletRepository walletRepository){
        this.walletRepository=walletRepository;
    }

    @Override
    public Wallet save(Wallet wallet) {
        return walletRepository.save(wallet);
    }

    @Override
    public Wallet getWalletById(Long id) {
        return walletRepository.findById(id).orElse(null);
    }

    @Override
    public List<Wallet> getAllWallets() {
        return (List<Wallet>) walletRepository.findAll();
    }

    @Override
    public void deleteById(Long id) {
        walletRepository.deleteById(id);
    }
}
