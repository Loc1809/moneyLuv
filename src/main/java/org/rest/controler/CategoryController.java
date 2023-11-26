package org.rest.controler;

import com.fasterxml.jackson.databind.JsonNode;
import net.bytebuddy.implementation.bytecode.Throw;
import org.rest.Service.UserService;
import org.rest.model.Category;
import org.rest.model.User;
import org.rest.repository.CategoryRepository;
import org.rest.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@CrossOrigin
@RequestMapping("/category")
public class CategoryController {

    Environment environment;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    UserRepository userRepository;

    public CategoryController(Environment environment) {
        this.environment = environment;
    }

    @GetMapping("/{type}")
    public ResponseEntity<Object> getAllCategory(@PathVariable ("type") String type,
                                                 @RequestParam(value = "page", required = false) Integer page,
                                                 @RequestParam (value = "size", required = false) Integer size,
                                                 HttpServletRequest req) throws AuthenticationException {
        int direction = (type.equals("income")) ? 0 : 1;
        Pageable pageable = (page != null) ? PageRequest.of(page, size) : PageRequest.of(0, Integer.MAX_VALUE);
        int[] users = new int[]{0, new UserService(environment).getCurrentUserId(req, userRepository)};

        return new ResponseEntity<>( categoryRepository.getCategoriesByUserIsInAndType(users, direction), HttpStatus.OK);
//        return new ResponseEntity<>( categoryRepository.findAllByTypeAndUser(direction, getCurrentUser(req, userRepository), pageable), HttpStatus.OK);
    }

    @GetMapping("/search/{param}")
    public ResponseEntity<Object> findCategoryByIdOrName(@PathVariable String param
//                                                       , @RequestParam(value = "page", required = false) Integer page,
//                                                          @RequestParam (value = "size", required = false) Integer size
                                                         ){
//        Pageable pageable = (page != null) ? PageRequest.of(page, size) : PageRequest.of(0, Integer.MAX_VALUE);
        return new ResponseEntity<>( categoryRepository.findByIdOrName(Integer.parseInt(param), param), HttpStatus.OK);
    }

    @PutMapping("/update/{id}")
//   LOC NOTE: USER
    public ResponseEntity<Object> updateCategoryById(@PathVariable String id, @RequestBody Category category, HttpServletRequest req) throws AuthenticationException {
        Optional<Category> categoryFound = categoryRepository.findByIdAndUser(Integer.parseInt(id),
                new UserService(environment).getCurrentUserId(req, userRepository));
        if (categoryFound.isPresent()){
            Category oldOne = categoryFound.get();
            oldOne.setName(category.getName());
            oldOne.setIcon(category.getIcon());
            oldOne.setActive(category.getActive());
            return new ResponseEntity<>(categoryRepository.save(oldOne), HttpStatus.OK);
        }
        else
            return new ResponseEntity<>("Transaction Type not found", HttpStatus.NOT_FOUND);
    }

    @PostMapping("/create/{type}")
//    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Object> createNewCategory(@PathVariable ("type") String type, @RequestBody JsonNode category,
                                                    HttpServletRequest request){
        try {
            int direction = (type.equals("income")) ? 0 : 1;
            Optional<Category> categoryFound = categoryRepository.findByNameAndTypeAndUser(category.get("name").asText(),
                    direction, new UserService(environment).getCurrentUserId(request, userRepository));
            if (categoryFound.isPresent())
                return new ResponseEntity<>("This category already existed", HttpStatus.CONFLICT);
            else{
                int parentId = (category.get("parent") == null) ? 0 : category.get("parent").asInt();
                String icon = (category.get("icon") == null) ? "" : category.get("parent").asText();
                Category newCategory = new Category(category.get("name").asText(), direction, List.of(),
                        new UserService(environment).getCurrentUserId(request, userRepository), true, icon);
                if (parentId != 0)
                    updateChildCategory(newCategory, parentId);
                return new ResponseEntity<>(categoryRepository.save(newCategory), HttpStatus.OK);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public void updateChildCategory(Category category,int parentId){
        try {
            Category parentCate = categoryRepository.findById(parentId).get();
            if (Objects.equals(parentCate.getType(), category.getType()))
                parentCate.addChildCategory(category);
            else
                throw new IllegalStateException ("Error: Child and parent should be of the same type.");
        } catch (Throwable e) {

            System.out.println(e.getMessage());
        }
    }

    public User getCurrentUser(HttpServletRequest request, UserRepository userRepository){
        UserService userService = new UserService(environment);
        return userService.getCurrentUser(request, userRepository);
    }
}
