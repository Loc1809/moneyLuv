package org.rest.repository;


import org.rest.model.IncomeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IncomeTypeRepository extends JpaRepository<org.rest.model.IncomeType, String>{
    Optional<IncomeType> findByIdOrName(String id, String name);
    Optional<IncomeType> findByName(String name);
}