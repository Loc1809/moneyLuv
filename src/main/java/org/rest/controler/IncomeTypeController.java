package org.rest.controler;

import org.apache.coyote.Response;
import org.rest.model.IncomeType;
import org.rest.repository.IncomeTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@CrossOrigin
@RequestMapping("/incometype")
public class IncomeTypeController {
    @Autowired
    IncomeTypeRepository incomeTypeRepository;

    @GetMapping("/all")
    public ResponseEntity<Object> getAllIncomeType(){
        return new ResponseEntity<>( incomeTypeRepository.findAll(), HttpStatus.OK);
    }

    @GetMapping("/search/{param}")
    public ResponseEntity<Object> findIncomeTypeByIdOrName(@PathVariable String param){
        return new ResponseEntity<>( incomeTypeRepository.findByIdOrName(param, param), HttpStatus.OK );
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Object> updateIncomeTypeById(@PathVariable String id, @RequestBody IncomeType incomeType){
        Optional<IncomeType> typeFound = incomeTypeRepository.findByName(incomeType.getName());
        if (typeFound.isPresent())
            return new ResponseEntity<>(incomeTypeRepository.save(incomeType), HttpStatus.OK);
        else
            return new ResponseEntity<>("Income Type not found", HttpStatus.NOT_FOUND);
    }

    @PostMapping("/create")
    public ResponseEntity<Object> createNewIncomeType(@RequestBody IncomeType incomeType){
        Optional<IncomeType> typeFound = incomeTypeRepository.findByName(incomeType.getName());
        if (typeFound.isPresent())
            return new ResponseEntity<>("This income type has already existed", HttpStatus.CONFLICT);
        else
            return new ResponseEntity<>(incomeTypeRepository.save(incomeType), HttpStatus.OK);
    }
}
