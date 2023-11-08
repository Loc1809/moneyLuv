package org.rest.controler;

import org.rest.model.Category;
import org.rest.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@CrossOrigin
@RequestMapping("/transactiontype")
public class CategoryController {
    @Autowired
    CategoryRepository transactionTypeRepository;

    @GetMapping("/all")
    public ResponseEntity<Object> getAllCategory(){
        return new ResponseEntity<>( transactionTypeRepository.findAll(), HttpStatus.OK);
    }

    @GetMapping("/search/{param}")
    public ResponseEntity<Object> findCategoryByIdOrName(@PathVariable Integer param){
        return new ResponseEntity<>( transactionTypeRepository.findByIdOrName(param, param.toString()), HttpStatus.OK );
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Object> updateCategoryById(@PathVariable String id, @RequestBody Category transactionType){
        Optional<Category> typeFound = transactionTypeRepository.findByName(transactionType.getName());
        if (typeFound.isPresent())
            return new ResponseEntity<>(transactionTypeRepository.save(transactionType), HttpStatus.OK);
        else
            return new ResponseEntity<>("Transaction Type not found", HttpStatus.NOT_FOUND);
    }

    @PostMapping("/create")
    public ResponseEntity<Object> createNewCategory(@RequestBody Category transactionType){
        Optional<Category> typeFound = transactionTypeRepository.findByName(transactionType.getName());
        if (typeFound.isPresent())
            return new ResponseEntity<>("This transaction type has already existed", HttpStatus.CONFLICT);
        else
            return new ResponseEntity<>(transactionTypeRepository.save(transactionType), HttpStatus.OK);
    }
}
