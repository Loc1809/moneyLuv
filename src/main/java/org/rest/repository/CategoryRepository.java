package org.rest.repository;


import org.rest.model.Category;
import org.rest.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.util.List;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    Optional<Category> findByIdOrName(Integer id, String name);
    Optional<Category> findByNameAndType(String name, int type);

    List<Category> findAllByUser(User user, Pageable pageable);

    Page<Category> findAllByTypeAndUser(int type, User user, Pageable pageable);
}