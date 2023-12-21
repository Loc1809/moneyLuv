package org.rest.controler;

import com.fasterxml.jackson.databind.JsonNode;
import org.json.JSONException;
import org.json.JSONObject;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;

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

    @GetMapping("/calculate")
    public ResponseEntity<Object> calculateUserSaving(HttpServletRequest req, HttpServletResponse res) throws AuthenticationException, JSONException {
        User user = new UserService(environment).getCurrentUser(req, userRepository);
        int[] users = new int[]{0, new UserService(environment).getCurrentUserId(req, userRepository)};
        List<Saving> savingList = savingRepository.getSavingByUserAndActive(user, true);
        Double savingAmount = 0.0;
        Double currentMoney = user.getMoney();
        Double interest = 0.0;
        Map<String, Float> baseRate = new HashMap<>();
        for (Saving saving : savingList){
            savingAmount += saving.getAmount();
            interest += calculateInterest(saving, baseRate);
        }
        JSONObject response = new JSONObject();
        response.put("savingAmount", savingAmount);
        response.put("interest", interest);
        response.put("currentMoney", currentMoney);
        res.setContentType("application/json");
        return new ResponseEntity<>(response.toString(), HttpStatus.OK);
    }

    @PostMapping("/create")
    public ResponseEntity<Object> createSaving(@RequestBody JsonNode jsonNode, HttpServletRequest req){
        try {
            UserService userService = new UserService(environment);
            User user = userService.getCurrentUser(req, userRepository);
            String startDate = convertStringToEpoch(jsonNode.get("startDate").asText());
            String endDate = (jsonNode.get("endDate").asText().equals("")) ? "9999999999000" : convertStringToEpoch(jsonNode.get("endDate").asText());
            BankInfo bankInfo = bankInfoRepository.findById(jsonNode.get("bankInfo").asInt()).get();
            TransientBankInfo transientBank = new TransientBankInfo(bankInfo.getBankName(), bankInfo.getInterestRate(), bankInfo.getTerm());
            if (!List.of(0, user.getId()).contains(bankInfo.getUser()))
                return new ResponseEntity<>("Bank infor not valid", HttpStatus.BAD_REQUEST);
            Saving saving = new Saving(jsonNode.get("amount").floatValue(), startDate,
                    endDate, jsonNode.get("desc").asText(), user, transientRepository.save(transientBank),true,
                    String.valueOf(Instant.now().toEpochMilli()));
            userService.updateUserAmount(user, saving.getAmount(), 1, userRepository);
            saving.setUpdatedDate(String.valueOf(Instant.now().toEpochMilli()));
            return new ResponseEntity<>(savingRepository.save(saving), HttpStatus.OK);
        } catch (DataIntegrityViolationException dupplicate){
            return new ResponseEntity<>("Database error, please try again", HttpStatus.CONFLICT);
        }
        catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/takemoney/{id}")
    public ResponseEntity<Object> takeSaving(@PathVariable ("id") String id, HttpServletRequest request){
         try {
             UserService userService = new UserService(environment);
             User user = userService.getCurrentUser(request, userRepository);
             Optional<Saving> savingFound = savingRepository.findSavingByIdAndUser(Integer.parseInt(id), user);
             if (savingFound.isPresent()){
                 Saving curSaving = savingFound.get();
                 curSaving.setStatus(false);
                 double money = calculateInterest(curSaving, new HashMap<>()) + curSaving.getAmount();
                 userService.updateUserAmount(user, money, 0, userRepository);
                 return new ResponseEntity<>(savingRepository.save(curSaving), HttpStatus.OK);
             } else
                return new ResponseEntity<>("Not found", HttpStatus.NOT_FOUND);
        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Object> updateSaving(@RequestBody Saving saving, @PathVariable ("id") String id, HttpServletRequest request){
        try {
            UserService userService = new UserService(environment);
            User user = userService.getCurrentUser(request, userRepository);
            Optional<Saving> savingFound = savingRepository.findSavingByIdAndUser(Integer.parseInt(id), user);
            if (savingFound.isPresent()){
                Saving curSaving = savingFound.get();
                curSaving.setAmount(saving.getAmount());
                curSaving.setDesc(saving.getDesc());
                curSaving.setStartDate(convertStringToEpoch(saving.startDate()));
                curSaving.setUpdatedDate(String.valueOf(Instant.now().toEpochMilli()));
                userService.updateUserAmount(user, saving.getAmount() - curSaving.getAmount()
                            , 1, userRepository);
                return new ResponseEntity<>(savingRepository.save(curSaving), HttpStatus.OK);
            } else
                return new ResponseEntity<>("Not found", HttpStatus.NOT_FOUND);
        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/delete/{id}")
    public ResponseEntity<Object> deleteSaving(@PathVariable ("id") String id, HttpServletRequest request){
         try {
            UserService userService = new UserService(environment);
            User user = userService.getCurrentUser(request, userRepository);
            Optional<Saving> savingFound = savingRepository.findSavingByIdAndUser(Integer.parseInt(id), user);
            if (savingFound.isPresent()){
                Saving curSaving = savingFound.get();
                curSaving.setStatus(false);
                userService.updateUserAmount(user, curSaving.getAmount(), 0, userRepository);
                return new ResponseEntity<>(savingRepository.save(curSaving), HttpStatus.OK);
            } else
                return new ResponseEntity<>("Not found", HttpStatus.NOT_FOUND);
        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public double calculateInterest(Saving saving, Map<String, Float> baseRates){
        double result = 0;
        String bankName = saving.getBankInfo().getBankName();
        if (bankInfoRepository.getBankInfoByBankName(bankName).get(0).getUser() == 0){
            int currentTerm = saving.getBankInfo().getTerm();
            long startDate = Long.parseLong(saving.startDate())/1000;
            long endDate = startDate + (long) currentTerm *2629743;
            long actualEndDate = Instant.now().toEpochMilli()/1000;
            int daysPassed = (int) Math.floorDiv(actualEndDate - endDate, (86400));
            if (baseRates.get(bankName) == null)
                baseRates.put(bankName, bankInfoRepository.getBankInfoByBankNameAndTerm
                        (bankName, -1).get(0).getInterestRate()/365);
            if (currentTerm != -1){
                if (actualEndDate < endDate){
                    result = saving.getAmount() * (( baseRates.get(bankName) * daysPassed));
                } else {
                    double rate = saving.getBankInfo().getInterestRate();
                    long timePassed = (actualEndDate - startDate);
                    int cycleDone = (int) Math.floorDiv(timePassed, 2629743*currentTerm);
                    int days = (int) Math.floorDiv(timePassed % (2629743 * currentTerm), 86400);
                    result = saving.getAmount() * (rate*(currentTerm/12)*cycleDone + days * baseRates.get(bankName));
                }
            } else {
                result = daysPassed*baseRates.get(bankName);
            }
            return result;
        } else {
            return 0;
        }
    }

    public static String convertStringToEpoch(String timeStr) throws ParseException {
        try {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            df.setTimeZone(TimeZone.getTimeZone(ZoneId.systemDefault()));
            Date date = df.parse(timeStr);
            return String.valueOf(date.getTime());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "";
        }
    }
}
