package org.rest.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SavingTypeRepository extends JpaRepository<org.rest.model.SavingType, Integer>{

}