package com.marcosoft.storageSoftware.repository;

import com.marcosoft.storageSoftware.domain.Investment;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface InvestmentRepository extends CrudRepository<Investment, Long> {
    List<Investment> findByIsAssigned(Boolean isAssigned);
}