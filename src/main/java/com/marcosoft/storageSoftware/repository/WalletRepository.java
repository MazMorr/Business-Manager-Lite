package com.marcosoft.storageSoftware.repository;

import com.marcosoft.storageSoftware.model.Wallet;
import org.springframework.data.repository.CrudRepository;

public interface WalletRepository extends CrudRepository<Wallet, Long> {
}