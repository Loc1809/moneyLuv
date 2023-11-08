package org.rest.controler;

import com.fasterxml.jackson.databind.JsonNode;
import org.rest.Service.UserService;
import org.rest.model.Transaction;
import org.rest.repository.TransactionRepository;
import org.rest.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@CrossOrigin
@RestController
@RequestMapping("/transaction")
public class TransactionController {

//    LOC NOTE: Common function:
//    Convert epoch to datetime format
//    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
//
//    Instant.ofEpochSecond(epochTime).atZone(ZoneId.systemDefault()).toLocalDateTime().format(dtf);

    UserService userService;

    @Autowired
    CategoryRepository transactionTypeRepository;

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    EntityManager entityManager;

    @GetMapping("/")
    public ResponseEntity<Object> getAllTransaction(){
        return new ResponseEntity<>(transactionRepository.findAll(), HttpStatus.OK);
    }

    @GetMapping("/statistic")
    public ResponseEntity<Object> searchTransaction(@RequestParam (value = "size", defaultValue = "10") String pageSize,
                                               @RequestParam (value = "page", defaultValue = "1") String page,
                                               @RequestParam (value = "query", defaultValue = "") String query, // query=col1,col2,col3
                                               @RequestParam (value = "start_date", defaultValue = "") String startDate,
                                               @RequestParam (value = "end_date", defaultValue = "") String endDate,
                                               HttpServletRequest req){
        try {
            List<String> defaultValues = new ArrayList(Arrays.asList(/*size*/"10", /*page*/"1", /*query*/"", /*time*/"", ""));
            StringBuilder baseQuery = new StringBuilder("SELECT * FROM transaction WHERE ");
            String baseWhere = "1";
            boolean isSelectWhere = true;
            if (!query.equals(defaultValues.get(2))){
                List<String> queries = List.of(query.split(","));
                baseQuery = new StringBuilder("SELECT ");
                for (String tmp : queries){
                    baseQuery.append(tmp).append(", ");
                }
                baseQuery.deleteCharAt(baseQuery.lastIndexOf(","));
                baseQuery.append("FROM transaction");
                isSelectWhere = false;
            }
            if (isSelectWhere)
                baseQuery.append(" 1 ");
            if (!startDate.equals("")){

            }

    //        Datetime query for later

//            baseQuery.append(" LIMIT ").append(pageSize);
//            baseQuery.append(" OFFSET ").append(Integer.parseInt(page) -1 );
//            transactionRepository.queryCriteria(baseQuery.toString());
//            List<Transaction> results = createTransactionCriteria(baseQuery.toString());
            List<Transaction> transactions = createTransactionCriteria(startDate, endDate, Arrays.asList(1, 2));
            return new ResponseEntity<>(createTransactionCriteria(startDate, endDate, Arrays.asList(1, 2)), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<Object> findTransactionByMoreThanOneAttribute(@RequestParam ("param") String param){
        String query = "SELECT * FROM transaction WHERE ";
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/search/{id}")
    public ResponseEntity<Object> findTransactionById(@PathVariable Integer id){
        Optional<Transaction> isFound = transactionRepository.findById(id);
        if (isFound.isPresent())
            return new ResponseEntity<>(isFound.get(), HttpStatus.OK);
        else
            return new ResponseEntity<>("Transaction Type not found", HttpStatus.NOT_FOUND);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Object> updateTransactionById(@PathVariable Integer id, @RequestBody Transaction transaction){
        Optional<Transaction> isFound = transactionRepository.findById(id);
        if (isFound.isPresent()) {
            Transaction transactionFound = isFound.get();
            transactionFound.setAmount(transaction.getAmount());
            transactionFound.setDesc(transaction.getDesc());
            transactionFound.setTime(transaction.getTime());
            // Not allow to update source and type
            return new ResponseEntity<>(transactionRepository.save(transaction), HttpStatus.OK);
        } else
            return new ResponseEntity<>("Transaction not found", HttpStatus.NOT_FOUND);
    }

    @PostMapping("/create")
    public ResponseEntity<Object> createTransaction(@RequestBody JsonNode transactionNode, HttpServletRequest request){
        try{
            Transaction transaction = new Transaction( transactionNode.get("amount").floatValue(), convertStringToEpoch(transactionNode.get("time").asText()).toString(),
                    transactionNode.get("desc").asText(), transactionTypeRepository.findById(transactionNode.get("category").asInt()).get(),
                    userService.getCurrentUserId(request), transactionNode.get("direction").asInt());
//                    userService.getCurrentUserId(request));
            return new ResponseEntity<>(transactionRepository.save(transaction), HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public List<Transaction> createTransactionCriteria(String startDate, String endDate,  List<Integer> categories){
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Transaction> query = cb.createQuery(Transaction.class);
        Root<Transaction> root = query.from(Transaction.class);

        if (!startDate.equals("")){
            Predicate likePredicate = cb.between(root.get("time"),  startDate, endDate);
            query.where(likePredicate);
        }
//        if (criteria != null && !criteria.isEmpty()) {
//            // Example: Creating a dynamic 'LIKE' condition
//            Predicate likePredicate = cb.like(root.get("propertyName"), "%" + criteria + "%");
//            query.where(likePredicate);
//        }
        TypedQuery<Transaction> typedQuery = entityManager.createQuery(query);
        return typedQuery.getResultList();
    }

    public String convertEpochToDateString(Long epoch){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return Instant.ofEpochMilli(epoch).atZone(ZoneId.systemDefault()).toLocalDateTime().format(dtf);
    }

    public Long convertStringToEpoch(String timeStr) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone(ZoneId.systemDefault()));
        Date date = df.parse(timeStr);
        return date.getTime();
    }
}
