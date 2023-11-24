package org.rest.repository;

import org.rest.model.FinanceGoal;
import org.rest.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FinanceGoalRepository extends JpaRepository<FinanceGoal, Integer> {
    List<FinanceGoal> getFinanceGoalByUser(User user);

     @Query(value = "SELECT * FROM finance_goal WHERE " +
            "(start_date BETWEEN  :start AND :end) AND (end_date BETWEEN :start AND :end)  " +
             "AND user = :user AND active = true", nativeQuery = true)
    List<FinanceGoal> findFinanceGoalByTime(String start, String end, User user);
}
