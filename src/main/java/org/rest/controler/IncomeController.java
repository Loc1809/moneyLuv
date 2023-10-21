package org.rest.controler;

import org.rest.model.Income;
import org.rest.repository.IncomeRepository;
import org.rest.repository.IncomeTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@CrossOrigin
@RestController
@RequestMapping("/income")
public class IncomeController {
    @Autowired
    IncomeTypeRepository incomeTypeRepository;

    @Autowired
    IncomeRepository incomeRepository;

    @GetMapping("/")
    public ResponseEntity<Object> getAllIncome(){
        return new ResponseEntity<>(incomeRepository.findAll(), HttpStatus.OK);
    }

    @GetMapping("/search/{id}")
    public ResponseEntity<Object> findIncomeById(@PathVariable Integer id){
        Optional<Income> isFound = incomeRepository.findById(id);
        if (isFound.isPresent())
            return new ResponseEntity<>(isFound.get(), HttpStatus.OK);
        else
            return new ResponseEntity<>("Income Type not found", HttpStatus.NOT_FOUND);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Object> updateIncomeById(@PathVariable Integer id, @RequestBody Income income){
        Optional<Income> isFound = incomeRepository.findById(id);
        if (isFound.isPresent())
            return new ResponseEntity<>(incomeRepository.save(income), HttpStatus.OK);
        else
            return new ResponseEntity<>("Income not found", HttpStatus.NOT_FOUND);
    }

    @PostMapping("/create")
    public ResponseEntity<Object> createIncome(@RequestBody Income income){
        try{
            return new ResponseEntity<>(incomeRepository.save(income), HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
