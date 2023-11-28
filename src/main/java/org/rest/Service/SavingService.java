package org.rest.Service;

import org.rest.model.BankInfo;
import org.rest.model.Saving;
import org.rest.repository.SavingRepository;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.List;

@Service
public class SavingService {
    private final Environment environment;

    public SavingService(Environment environment) {
        this.environment = environment;
    }

    private final List<Integer> TERM = Arrays.asList(-1, 1, 3, 6, 9, 12, 13, 18, 24, 36);

    public void updatePreviousSavingInDay(Map<String, String[]> bankInfo, String startOfDay, String updatedDate, SavingRepository savingRepository){
        try {
            List<Saving> previousSaving = savingRepository.getSavingByUpdatedDateBetween(startOfDay, updatedDate);
            for (Saving saved : previousSaving){
                BankInfo oldOne = saved.getBankInfo();
                String[] rates = bankInfo.get(oldOne.getBankName());
                saved.setUpdatedDate(String.valueOf(Instant.now().toEpochMilli()));
                oldOne.setInterestRate(Float.parseFloat(rates[TERM.indexOf(oldOne.getTerm())]));
                saved.setBankInfo(oldOne);
            }
            savingRepository.saveAll(previousSaving);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
