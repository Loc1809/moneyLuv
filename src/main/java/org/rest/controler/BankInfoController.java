package org.rest.controler;

import com.fasterxml.jackson.databind.JsonNode;
import org.rest.Service.SavingService;
import org.rest.Service.UserService;
import org.rest.model.BankInfo;
import org.rest.model.Saving;
import org.rest.model.User;
import org.rest.repository.BankInfoRepository;
import org.rest.repository.SavingRepository;
import org.rest.repository.TransientRepository;
import org.rest.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.naming.AuthenticationException;
import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
@RestController
@CrossOrigin
@RequestMapping("/bankinfo")
public class BankInfoController {
    @Autowired
    BankInfoRepository bankInfoRepository;

    @Autowired
    SavingRepository savingRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TransientRepository transientRepository;

    Environment environment;

    public BankInfoController(Environment environment){
        this.environment = environment;
    }

    private final List<Integer> TERM = Arrays.asList(-1, 1, 3, 6, 9, 12, 13, 18, 24, 36);

    @GetMapping("/")
    public ResponseEntity<Object> getAllBank(@RequestParam (value = "page", required = false) Integer page,
                                             @RequestParam (value = "size", required = false) Integer size,
                                             HttpServletRequest req) throws AuthenticationException {
        int[] users = new int[]{0, new UserService(environment).getCurrentUserId(req, userRepository)};
        Pageable pageable = (page != null) ? PageRequest.of(page, size) : PageRequest.of(0, Integer.MAX_VALUE);
        return new ResponseEntity<>(bankInfoRepository.getBankInfoByUserIsInAndActive(users,true, pageable), HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchBankInfo(@RequestParam ("query") String query, HttpServletRequest req) throws AuthenticationException {
        int[] users = new int[]{0, new UserService(environment).getCurrentUserId(req, userRepository)};
        return new ResponseEntity<>(bankInfoRepository.getBankInfoByBankNameContainingAndUserIsIn(query,
                users), HttpStatus.OK);
    }

    @PostMapping("/create")
    public ResponseEntity<Object> createBankInfo(@RequestBody JsonNode json, HttpServletRequest req){
        try{
            UserService userService = new UserService(environment);
            BankInfo bankInfo = new BankInfo(json.get("bankName").asText(), json.get("interestRate").floatValue(), json.get("term").asInt(), getCurrentEpoch());
            bankInfo.setLastUpdated(convertEpochToDateString(Instant.now().toEpochMilli()));
            bankInfo.setActive(true);
            bankInfo.setUser(userService.getCurrentUserId(req, userRepository));
            return new ResponseEntity<>(bankInfoRepository.save(bankInfo), HttpStatus.OK);
        } catch (DataIntegrityViolationException dupplicate){
            return new ResponseEntity<>("Database error, please try again", HttpStatus.CONFLICT);
        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Object> updateBankInfoById(@PathVariable ("id") String id, @RequestBody JsonNode json, HttpServletRequest req){
        try {
            User user = new UserService(environment).getCurrentUser(req, userRepository);
            Optional<BankInfo> bankInfoFound = bankInfoRepository.getBankInfoByIdAndUserAndActive(Integer.parseInt(id), user.getId(), true);
            if (bankInfoFound.isPresent()){
                BankInfo bankInfo = bankInfoFound.get();
                bankInfo.setBankName(json.get("bankName").asText());
                bankInfo.setTerm(json.get("term").asInt());
                bankInfo.setInterestRate(json.get("interestRate").floatValue());
                return new ResponseEntity<>(bankInfoRepository.save(bankInfo), HttpStatus.OK);
            } else
                return new ResponseEntity<>("Obj not found", HttpStatus.NOT_FOUND);
        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/delete/{id}")
    public ResponseEntity<Object> deleteBankInfo(@PathVariable ("id") String id, HttpServletRequest request){
         try {
            User user = new UserService(environment).getCurrentUser(request, userRepository);
            Optional<BankInfo> bankInfoFound = bankInfoRepository.getBankInfoByIdAndUserAndActive(Integer.parseInt(id), user.getId(), true);
            if (bankInfoFound.isPresent()){
                BankInfo curBankInfo = bankInfoFound.get();
                curBankInfo.setActive(false);
                return new ResponseEntity<>(bankInfoRepository.save(curBankInfo), HttpStatus.OK);
            } else
                return new ResponseEntity<>("Not found", HttpStatus.NOT_FOUND);
        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/test")
    public ResponseEntity<Object> test(){
        try {
            RestTemplate restTemplate = new RestTemplate();
            String from = environment.getProperty("bankinfo.url");
            String source = restTemplate.getForObject(from, String.class);
            Map<String, String[]> bankData = getDataFromExternal (source);

            return new ResponseEntity<>(bankData, HttpStatus.OK);
        } catch (Exception e ){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public Map<String, String[]> getDataFromExternal (String source) throws ParseException {
        Map<String, String[]> bankData = new LinkedHashMap<>();
        Document doc = Jsoup.parse(source);

        Element table = doc.select("table").get(0);
        Elements rows = table.select("tr");
        String updatedDate = checkUpdateDate(doc);
        if (updatedDate != null) {
            for (Element row : rows) {
                Elements columns = row.select("td.text-left a");
                if (columns.size() > 0) {
                    String bankName = columns.text();
                    Elements nbColumns = row.select("td.text-right");
                    String[] nbArray = nbColumns.stream()
                            .map(element -> parseNb(element.attr("nb")))
                            .toArray(String[]::new);
                    bankData.put(bankName, nbArray);
                }
            }
            updateAllBankInfo(bankData, updatedDate);
        }
        return bankData;
    }

    public String checkUpdateDate(Document doc) throws ParseException {
        Elements paragraphs = doc.select("p");
        for (Element paragraph : paragraphs) {
            if (paragraph.text().contains("Dữ liệu được cập nhật lúc")) {
                Pattern pattern = Pattern.compile("\\d{2}:\\d{2}:\\d{2} \\d{2}/\\d{2}/\\d{4}");
                Matcher matcher = pattern.matcher(paragraph.text());
                if (matcher.find()) {
                    String updatedDate = String.valueOf(convertStringToEpoch(matcher.group()));
//                    If current latestBankInfo exist & update_date > the update_date of server -> don't need to update
                    BankInfo latestBankInfo = bankInfoRepository.getFirstByUserAndLastUpdatedGreaterThan(0, "17008113200000");
                    if (latestBankInfo != null)
                        return null;
                    else
                        return matcher.group();
                }
                break;
            }
        }
        return null;
    }

    public void updateAllBankInfo(Map<String, String[]> banksRate, String updatedDate){
        List<BankInfo> banks = bankInfoRepository.getBankInfoByUserOrderByBankName(0);
        if (banks.size() != 0){
            for (BankInfo bankInfo : banks){
                bankInfo.setLastUpdated(updatedDate);
                bankInfo.setInterestRate(Float.parseFloat(banksRate.get(bankInfo.getBankName())[TERM.indexOf(bankInfo.getTerm())]));
            }
            SavingService savingService = new SavingService(environment);
            savingService.updatePreviousSavingInDay(banksRate, getCurrentEpoch(), updatedDate, savingRepository, transientRepository);
            bankInfoRepository.saveAll(banks);
        } else {
//            Don't have any banks
            for (Map.Entry<String, String[]> bankRate : banksRate.entrySet()){
                createBank(bankRate.getKey(), bankRate.getValue());
            }
        }
    }

    public void createBank(String bankName, String[] bankRate){
        List<BankInfo> bankInfos = new ArrayList<>();
        for (int i = 0; i < bankRate.length; i++) {
            if (bankRate[i].equals("-"))
                continue;
            bankInfos.add(new BankInfo(bankName, Float.parseFloat(bankRate[i]), TERM.get(i), getCurrentEpoch()));
        }
        bankInfoRepository.saveAll(bankInfos);
    }

    public String parseNb(String string) {
        string = string.replaceAll("[A-Z]", "");
        int len = string.length();
        byte[] byteArray = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            byteArray[i / 2] = (byte) ((Character.digit(string.charAt(i), 16) << 4)
                    + Character.digit(string.charAt(i + 1), 16));
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (int value : byteArray) {
            stringBuilder.append((char) value);
        }
        String rate = stringBuilder.toString().replace(",", ".");
        if (rate.contains("span"))
            rate = Jsoup.parse(rate).select("span").first().text();
        return rate;
    }


    public String convertEpochToDateString(Long epoch){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return Instant.ofEpochMilli(epoch).atZone(ZoneId.systemDefault()).toLocalDateTime().format(dtf);
    }

    public static Long convertStringToEpoch(String timeStr) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
        df.setTimeZone(TimeZone.getTimeZone(ZoneId.systemDefault()));
        Date date = df.parse(timeStr);
        return date.getTime();
    }

    public String getCurrentEpoch(){
        return String.valueOf(Instant.now().toEpochMilli());
    }
}
