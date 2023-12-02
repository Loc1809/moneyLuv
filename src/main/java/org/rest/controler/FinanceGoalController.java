package org.rest.controler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.rest.Service.TransactionService;
import org.rest.Service.UserService;
import org.rest.model.FinanceGoal;
import org.rest.model.Transaction;
import org.rest.model.User;
import org.rest.repository.FinanceGoalRepository;
import org.rest.repository.TransactionRepository;
import org.rest.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.net.FileNameMap;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.time.Instant;
@RestController
@CrossOrigin
@RequestMapping("/goal")
public class FinanceGoalController {
    @Autowired
    FinanceGoalRepository financeGoalRepository;

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    UserRepository userRepository;

    Environment environment;

    public FinanceGoalController(Environment environment) {
        this.environment = environment;
    }

    @GetMapping("/")
    public ResponseEntity<Object> getGoalByUser(HttpServletRequest request){
        try {
            UserService userService = new UserService(environment);
            return new ResponseEntity<>(financeGoalRepository.getFinanceGoalByUser(userService.getCurrentUser(request, userRepository)), HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/create")
    public ResponseEntity<Object> createGoal(@RequestBody JsonNode financeGoal, HttpServletRequest request){
        try {
            int direction = (financeGoal.get("type").asText().equals("income")) ? 0 : 1;
            UserService userService = new UserService(environment);
            Long startDate = convertStringToEpoch(financeGoal.get("startDate").asText());
            Long endDate = convertStringToEpoch(financeGoal.get("endDate").asText());
            FinanceGoal newGoal = new FinanceGoal(financeGoal.get("amount").floatValue(), startDate.toString(),
                    endDate.toString(), direction, userService.getCurrentUser(request, userRepository));
            newGoal.setActive(true);
            return new ResponseEntity<>(financeGoalRepository.save(newGoal), HttpStatus.OK);
        } catch (DataIntegrityViolationException dupplicate){
            return new ResponseEntity<>("Database error, please try again", HttpStatus.CONFLICT);
        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Object>updateGoal(@PathVariable ("id") String id, @RequestBody FinanceGoal financeGoal){
        try {
            Optional<FinanceGoal> found = financeGoalRepository.findById(Integer.parseInt(id));
            if (found.isPresent()) {
                FinanceGoal current = found.get();
                current.setActive(financeGoal.isActive());
                current.setAmount(financeGoal.getAmount());
                current.setStartDate(convertStringToEpoch(financeGoal.getStartDate()).toString());
                current.setEndDate(convertStringToEpoch(financeGoal.getEndDate()).toString());
                return new ResponseEntity<>(financeGoalRepository.save(current), HttpStatus.OK);
            }else
                return new ResponseEntity<>("Not found", HttpStatus.NOT_FOUND);
        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/statistic")
    public ResponseEntity<Object> getStatistic(@RequestParam (value = "startDate", required = false, defaultValue = "1900-01-01 00:00:00") String startDate,
                                               @RequestParam (value = "endDate", required = false, defaultValue = "") String endDate,
                                               HttpServletRequest req) throws ParseException {
        try {

            endDate = (endDate.equals("")) ? String.valueOf(Instant.now().toEpochMilli()) : String.valueOf(convertStringToEpoch(endDate));
            startDate = String.valueOf(convertStringToEpoch(startDate));
            UserService userService = new UserService(environment);
            User currentUser = userService.getCurrentUser(req, userRepository);
            List<FinanceGoal> goalList = financeGoalRepository.findFinanceGoalByTime(startDate, endDate, currentUser);
            List<JsonNode> result = new ArrayList<>();
            ObjectMapper mapper = new ObjectMapper();
            for (FinanceGoal goal : goalList) {
                String start = goal.getStartDate();
                String end = goal.getEndDate();
                float actual = 0;
                List<Transaction> transactionList = transactionRepository.findAllByTimeBetweenAndUser(start, end, currentUser);
                for (Transaction trans : transactionList) {
                    if (trans.actualDirection() == 0)
                        actual += trans.getAmount();
                    else
                        actual -= trans.getAmount();
                }
                ObjectNode node = mapper.convertValue(goal, ObjectNode.class);
                node.put("startDate", convertEpochToDateString(Long.parseLong(node.get("startDate").asText())));
                node.put("endDate", convertEpochToDateString(Long.parseLong(node.get("endDate").asText())));
                node.put("actual", actual);
                result.add(node);
            }
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    public Long convertStringToEpoch(String timeStr) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone(ZoneId.systemDefault()));
        Date date = df.parse(timeStr);
        return date.getTime();
    }

    public String convertEpochToDateString(Long epoch){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return Instant.ofEpochMilli(epoch).atZone(ZoneId.systemDefault()).toLocalDateTime().format(dtf);
    }
}
