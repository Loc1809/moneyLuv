package org.rest.repository;


import org.rest.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer>{
    Optional<Category> findByIdOrName(Integer id, String name);
    Optional<Category> findByName(String name);
}