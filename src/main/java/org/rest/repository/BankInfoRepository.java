package org.rest.repository;


import org.rest.model.BankInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface BankInfoRepository extends JpaRepository<org.rest.model.BankInfo, Integer>{
    List<BankInfo> getBankInfoByBankNameContainingAndUser (String bankName, int user);

    Page<BankInfo> getBankInfoByUserIsInAndActive(int[] user, boolean active, Pageable pageable);

    List<BankInfo> getBankInfoByUserOrderByBankName(int user);

    BankInfo getFirstByUserAndLastUpdatedGreaterThan(int user, String lastUpdated);
}