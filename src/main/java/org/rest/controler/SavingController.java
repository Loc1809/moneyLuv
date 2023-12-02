package org.rest.controler;

import com.fasterxml.jackson.databind.JsonNode;
import org.bouncycastle.jcajce.provider.asymmetric.ec.KeyFactorySpi;
import org.rest.Service.UserService;
import org.rest.model.BankInfo;
import org.rest.model.Saving;
import org.rest.model.TransientBankInfo;
import org.rest.model.User;
import org.rest.repository.BankInfoRepository;
import org.rest.repository.SavingRepository;
import org.rest.repository.TransientRepository;
import org.rest.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;
import javax.servlet.http.HttpServletRequest;
import java.sql.SQLDataException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

@RestController
@CrossOrigin
@RequestMapping("/saving")
public class SavingController {
    @Autowired
    SavingRepository savingRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    BankInfoRepository bankInfoRepository;

    @Autowired
    TransientRepository transientRepository;

    Environment environment;

    public SavingController(Environment environment){this.environment = environment;}

    @GetMapping("/")
    public ResponseEntity<Object> getSaving(HttpServletRequest req) throws AuthenticationException {
        User user = new UserService(environment).getCurrentUser(req, userRepository);
        int[] users = new int[]{0, new UserService(environment).getCurrentUserId(req, userRepository)};
        return new ResponseEntity<>(savingRepository.getSavingByUserAndActive(user, true), HttpStatus.OK);
    }

    @PostMapping("/create")
    public ResponseEntity<Object> createSaving(@RequestBody JsonNode jsonNode, HttpServletRequest req){
        try {
            User user = new UserService(environment).getCurrentUser(req, userRepository);
            String startDate = convertStringToEpoch(jsonNode.get("startDate").asText());
            String endDate = convertStringToEpoch(jsonNode.get("endDate").asText());
            BankInfo bankInfo = bankInfoRepository.findById(jsonNode.get("bankInfo").asInt()).get();
            TransientBankInfo transientBank = new TransientBankInfo(bankInfo.getBankName(), bankInfo.getInterestRate(), bankInfo.getTerm());
            if (!List.of(0, user.getId()).contains(bankInfo.getUser()))
                return new ResponseEntity<>("Bank infor not valid", HttpStatus.BAD_REQUEST);
            Saving saving = new Saving(jsonNode.get("amount").floatValue(), startDate,
                    endDate, jsonNode.get("desc").asText(), user, transientRepository.save(transientBank),true,
                    String.valueOf(Instant.now().toEpochMilli()));
            saving.setUpdatedDate(String.valueOf(Instant.now().toEpochMilli()));
            return new ResponseEntity<>(savingRepository.save(saving), HttpStatus.OK);
        } catch (DataIntegrityViolationException dupplicate){
            return new ResponseEntity<>("Database error, please try again", HttpStatus.CONFLICT);
        }
        catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Object> updateSaving(@RequestBody Saving saving, @PathVariable ("id") String id, HttpServletRequest request){
        try {
            User user = new UserService(environment).getCurrentUser(request, userRepository);
            Optional<Saving> savingFound = savingRepository.findSavingByIdAndUser(Integer.parseInt(id), user);
            if (savingFound.isPresent()){
                Saving curSaving = savingFound.get();
                curSaving.setAmount(saving.getAmount());
                curSaving.setDesc(saving.getDesc());
                curSaving.setStartDate(convertStringToEpoch(saving.startDate()));
                curSaving.setEndDate(convertStringToEpoch(saving.endDate()));
                curSaving.setStatus(saving.getStatus());
                curSaving.setUpdatedDate(String.valueOf(Instant.now().toEpochMilli()));
                return new ResponseEntity<>(savingRepository.save(curSaving), HttpStatus.OK);
            } else
                return new ResponseEntity<>("Not found", HttpStatus.NOT_FOUND);
        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public static String convertStringToEpoch(String timeStr) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone(ZoneId.systemDefault()));
        Date date = df.parse(timeStr);
        return String.valueOf(date.getTime());
    }


}
