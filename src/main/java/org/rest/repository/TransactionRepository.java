package org.rest.repository;


import org.rest.model.FinanceGoal;
import org.rest.model.Transaction;
import org.rest.model.User;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.swing.*;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer>{

    @Query(value = ":query", nativeQuery = true)
    List<Object[]> queryCriteria(@Param("query") String query);

    Optional<Transaction> findByIdAndUser(int id, User user);

    List<Transaction> findAllByDirection(int direction, Pageable pageable);

    List<Transaction> findAllByTimeBetweenAndUser(String start, String end, User user);

    List<Object[]> findAllByTimeBetweenAndCategoryContains(String timeBefore, String timeAfter, String category);

    @Query(value = "SELECT * FROM finance_goal WHERE " +
       "(start_date BETWEEN  :start AND :end) AND (end_date BETWEEN :start AND :end)  " +
        "AND user = :user AND active = true", nativeQuery = true)
    List<FinanceGoal> findTransactionByTime(String start, String end, User user);
}