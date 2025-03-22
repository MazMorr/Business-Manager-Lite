package com.marcosoft.storageSoftware.repository;

import com.marcosoft.storageSoftware.model.Wallet;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletRepository extends CrudRepository<Wallet, Long> {
}