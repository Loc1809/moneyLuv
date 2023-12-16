package org.rest.repository;


import org.rest.model.BankInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BankInfoRepository extends JpaRepository<org.rest.model.BankInfo, Integer>{
    List<BankInfo> getBankInfoByBankNameContainingAndUserIsIn(String bankName, int[] user);

    List<BankInfo> getBankInfoByBankNameAndTerm(String bankName, int term);

    Page<BankInfo> getBankInfoByUserIsInAndActive(int[] user, boolean active, Pageable pageable);

    Optional<BankInfo> getBankInfoByIdAndUserAndActive(int id, int user, boolean active);

    List<BankInfo> getBankInfoByUserOrderByBankName(int user);

    BankInfo getFirstByUserAndLastUpdatedGreaterThan(int user, String lastUpdated);
}