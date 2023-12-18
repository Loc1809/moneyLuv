package org.rest.repository;


import org.rest.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Integer>{
    User findUserByUsername(String username);
    User findUserByUsernameContains(String username);

    Optional<User> findUserByUsernameOrEmail(String username, String email);

    User getUserByUsername(String username);

    List<User> getUserByParent(User parent);
}