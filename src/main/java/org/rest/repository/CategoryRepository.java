package org.rest.repository;


import org.rest.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {


    Optional<Category> findByIdAndUserInAndActive(Integer id, Collection<Integer> user, boolean active);

    Optional<Category> findByIdAndUserAndActive(Integer id, int user, boolean active);

    Optional<Category> findByIdOrName(Integer id, String name);

    Category findByIdAndTypeAndUserInAndActive(int id, int type, Collection<Integer> user, Boolean active);


    List<Category> findByTypeAndUserInAndActive(int type, Collection<Integer> user, boolean active);

    List<Category> findAllByUser(int user, Pageable pageable);

    Page<Category> findAllByTypeAndUser(int type, int user, Pageable pageable);

    List<Category> getCategoriesByUserIsInAndTypeAndActive(int[] user, int type, boolean active);
}