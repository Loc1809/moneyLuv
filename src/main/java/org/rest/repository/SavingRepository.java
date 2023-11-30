package org.rest.repository;


import org.rest.model.Saving;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

import org.rest.model.User;
@Repository
public interface SavingRepository extends JpaRepository<org.rest.model.Saving, Integer>{
    List<Saving> getSavingByUserAndActive(User user, boolean active);

    Optional<Saving> findSavingByIdAndUser(int id, User user);

    List<Saving> getSavingByUpdatedDateBetween(String statrtDate, String endDate);
}