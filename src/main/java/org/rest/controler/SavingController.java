package org.rest.controler;

import com.fasterxml.jackson.databind.JsonNode;
import org.bouncycastle.jcajce.provider.asymmetric.ec.KeyFactorySpi;
import org.rest.Service.UserService;
import org.rest.model.BankInfo;
import org.rest.model.Saving;
import org.rest.model.User;
import org.rest.repository.BankInfoRepository;
import org.rest.repository.SavingRepository;
import org.rest.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;
import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Optional;

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
            BankInfo bankInfo = (BankInfo) bankInfoRepository.findById(jsonNode.get("bankInfo").asInt()).get().clone();
            Saving saving = new Saving(jsonNode.get("amount").floatValue(), jsonNode.get("startDate").asText(),
                    jsonNode.get("endDate").asText(), jsonNode.get("desc").asText(), user, bankInfo,true, String.valueOf(Instant.now().toEpochMilli()));
            savingRepository.save(saving);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e){
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
                curSaving.setStartDate(saving.getStartDate());
                curSaving.setEndDate(saving.getEndDate());
                curSaving.setStatus(saving.getStatus());
                return new ResponseEntity<>(savingRepository.save(curSaving), HttpStatus.OK);
            } else
                return new ResponseEntity<>("Not found", HttpStatus.NOT_FOUND);
        }   catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
