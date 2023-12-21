package org.rest.controler;

import com.fasterxml.jackson.databind.JsonNode;
import org.hibernate.query.criteria.internal.CriteriaBuilderImpl;
import org.rest.Service.UserService;
import org.rest.model.Category;
import org.rest.model.Transaction;
import org.rest.model.User;
import org.rest.repository.TransactionRepository;
import org.rest.repository.CategoryRepository;
import org.rest.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EntityManager entityManager;

    private final Environment environment;

    public TransactionController(Environment environment) {
        this.environment = environment;
    }

    @GetMapping("/{type}")
    public ResponseEntity<Object> getAllTransaction(@RequestParam(value = "page", required = false) Integer page,
                                                    @RequestParam (value = "size", required = false) Integer size,
                                                    @PathVariable ("type") String type, HttpServletRequest request){
        Pageable pageable = (page != null) ? PageRequest.of(page, size) : PageRequest.of(0, Integer.MAX_VALUE);
        User currentUser = new UserService(environment).getCurrentUser(request, userRepository);
        if (type.equals("income"))
            return new ResponseEntity<>(transactionRepository.findAllByDirectionAndUserAndActive(0, currentUser, true, pageable), HttpStatus.OK);
        return new ResponseEntity<>(transactionRepository.findAllByDirectionAndUserAndActive(1, currentUser, true, pageable), HttpStatus.OK);
    }

    @GetMapping("/statistic/{type}")
    public ResponseEntity<Object> searchTransaction(@PathVariable (value = "type", required = false, name = "") String type,
                                                    @RequestParam (value = "size", defaultValue = "10") String pageSize,
                                                    @RequestParam (value = "page", defaultValue = "1") String page,
                                                    @RequestParam (value = "cate", defaultValue = "") String cate, // cate=cate1,cate2,cate3
                                                    @RequestParam (value = "start_date", defaultValue = "") String startDate,
                                                    @RequestParam (value = "end_date", defaultValue = "")String endDate,
                                                    @RequestParam (value = "child", defaultValue = "") String childId,
                                                    HttpServletRequest req){
        try {
            int direction = (type.equals("income")) ? 0 : 1;
            endDate = (endDate.equals("")) ? String.valueOf(Instant.now().toEpochMilli()) : String.valueOf(convertStringToEpoch(endDate));
            startDate = (startDate.equals("")) ? String.valueOf(Instant.now().toEpochMilli()) : String.valueOf(convertStringToEpoch(startDate));
            User currentUser = new UserService(environment).getCurrentUser(req, userRepository);
            List<Category> categories = new ArrayList<>();
            List<Integer> users =List.of(0, currentUser.getId());
            if (!childId.equals("")){
                Optional<User> childUser = userRepository.findById(Integer.parseInt(childId));
                if (childUser.isPresent()){
                    if (!(childUser.get().parent().getId() == currentUser.getId()))
                        return new ResponseEntity<>("You can only view information of your child.", HttpStatus.FORBIDDEN);
                    users = List.of(0, childUser.get().getId());
                    currentUser = childUser.get();
                } else
                    return new ResponseEntity<>("Error while retrieving child account", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            if (!cate.equals("")){
                String[] values = cate.split(",");
                for (String value : values){
                    categories.add(categoryRepository.findByIdAndTypeAndUserInAndActive(Integer.parseInt(value), direction, users, true));
                }
            } else{
                categories = categoryRepository.findByTypeAndUserInAndActive(direction, users, true);
            }
            List<Transaction> statistics = transactionRepository.getTransactionByCategoryInAndTimeBetweenAndUserAndDirectionAndActive
                    (categories, startDate, endDate, currentUser, direction, true);
            return new ResponseEntity<>(statistics, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

//    @GetMapping("/search")
//    public ResponseEntity<Object> findTransactionByMoreThanOneAttribute(@RequestParam ("param") String param){
//        String query = "SELECT * FROM transaction WHERE ";
//        return new ResponseEntity<>(HttpStatus.OK);
//    }

    @GetMapping("/search/{id}")
    public ResponseEntity<Object> findTransactionById(@PathVariable Integer id){
        Optional<Transaction> isFound = transactionRepository.findById(id);
        if (isFound.isPresent())
            return new ResponseEntity<>(isFound.get(), HttpStatus.OK);
        else
            return new ResponseEntity<>("Transaction Type not found", HttpStatus.NOT_FOUND);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Object> updateTransactionById(@PathVariable ("id") String id, @RequestBody JsonNode transaction,
                                                        HttpServletRequest req){
        try {
            UserService userService = new UserService(environment);
            User user = userService.getCurrentUser(req, userRepository);
            Optional<Transaction> isFound = transactionRepository.findByIdAndUser(Integer.parseInt(id), user);
            if (isFound.isPresent()) {
                Transaction transactionFound = isFound.get();
                userService.updateUserAmount(user, transaction.get("amount").doubleValue() - transactionFound.getAmount()
                        , transactionFound.direction(), userRepository);
                transactionFound.setAmount(transaction.get("amount").doubleValue());
                transactionFound.setDesc(transaction.get("desc").asText());
                transactionFound.setTime(convertStringToEpoch(transaction.get("time").asText()).toString());
                // Not allow to update source and type
                 return new ResponseEntity<>(transactionRepository.save(transactionFound), HttpStatus.OK);
            } else
                return new ResponseEntity<>("Transaction not found", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/create/{type}")
    public ResponseEntity<Object> createTransaction(@PathVariable ("type") String type, @RequestBody JsonNode transactionNode, HttpServletRequest request){
        try{
            UserService userService = new UserService(environment);
            User user = userService.getCurrentUser(request, userRepository);

            int direction = (type.equals("income")) ? 0 : 1;
            Category category = categoryRepository.findByIdAndUserInAndActive(transactionNode.get("category").asInt(), List.of(0, user.getId()), true).get();
            if (category == null)
                return new ResponseEntity<>("Invalid category", HttpStatus.BAD_REQUEST);
//            Amount, time, desc, category, user, direction
            if (!category.getType().equals(type))
                return new ResponseEntity<>("Invalid category", HttpStatus.BAD_REQUEST);
            Transaction transaction = new Transaction( transactionNode.get("amount").doubleValue(), convertStringToEpoch(transactionNode.get("time").asText()).toString(),
                    transactionNode.get("desc").asText(), categoryRepository.findById(transactionNode.get("category").asInt()).get(),
                    user, direction);
            userService.updateUserAmount(user, transaction.getAmount(), direction, userRepository);
            return new ResponseEntity<>(transactionRepository.save(transaction), HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/delete/{id}")
//    Disable forever
    public ResponseEntity<Object> deleteTransaction(@PathVariable ("id") String id, HttpServletRequest request){
        try {
            UserService userService = new UserService(environment);
            User user = userService.getCurrentUser(request, userRepository);
            Optional<Transaction> isFound = transactionRepository.findByIdAndUser(Integer.parseInt(id), user);
            if (isFound.isPresent()) {
                Transaction transactionFound = isFound.get();
                transactionFound.setActive(false);
                userService.updateUserAmount(user, transactionFound.getAmount()*-1, transactionFound.direction(), userRepository);
                 return new ResponseEntity<>(transactionRepository.save(transactionFound), HttpStatus.OK);
            } else
                return new ResponseEntity<>("Transaction not found", HttpStatus.NOT_FOUND);
        } catch (DataIntegrityViolationException dupplicate){
            return new ResponseEntity<>("Database error, please try again", HttpStatus.CONFLICT);
        } catch (Exception e) {
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

        Predicate inPredicate = ((CriteriaBuilderImpl) cb).in(root.get("category"), categories);
        query.where(inPredicate);
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
