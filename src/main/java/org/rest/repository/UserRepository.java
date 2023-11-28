package org.rest.repository;


import org.rest.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer>{
    User findUserByUsernameContains(String username);

    Optional<User> findUserByUsernameOrEmail(String username, String email);

    User getUserByUsername(String username);
}