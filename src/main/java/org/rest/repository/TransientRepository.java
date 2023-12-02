package org.rest.repository;

import org.rest.model.TransientBankInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransientRepository extends JpaRepository<TransientBankInfo, Integer> {

}
