package org.rest.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<org.rest.model.Transaction, Integer>{

    @Query(value = ":query", nativeQuery = true)
    List<Object[]> queryCriteria(@Param("query") String query);

//    @Query(value = "SELECT SUM(count), loai_xe FROM `ve_thang_counter` WHERE thang >= :start AND nam >= :startY AND " +
//        "thang <= :end AND nam <= :endY GROUP BY loai_xe", nativeQuery = true)
//    List<Object[]> getDoanhThu(@Param("start") String start, @Param("end") String end, @Param("startY") String startY, @Param("endY") String endY);
}