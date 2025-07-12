package com.marcosoft.storageSoftware.repository;

import com.marcosoft.storageSoftware.domain.Investment;
import org.springframework.data.repository.CrudRepository;

public interface InvestmentRepository extends CrudRepository<Investment, Long> {
}