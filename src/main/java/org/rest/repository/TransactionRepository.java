package org.rest.repository;


import org.rest.model.Transaction;
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

    Optional<Transaction> findByIdAndUser(int id, int user);

    List<Transaction> findAllByDirection(int direction, Pageable pageable);

    List<Object[]> findAllByTimeBetweenAndCategoryContains(String timeBefore, String timeAfter, String category);
//    @Query(value = "SELECT SUM(count), loai_xe FROM `ve_thang_counter` WHERE thang >= :start AND nam >= :startY AND " +
//        "thang <= :end AND nam <= :endY GROUP BY loai_xe", nativeQuery = true)
//    List<Object[]> getDoanhThu(@Param("start") String start, @Param("end") String end, @Param("startY") String startY, @Param("endY") String endY);
}