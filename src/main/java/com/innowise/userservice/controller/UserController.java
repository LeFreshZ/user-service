package com.innowise.userservice.controller;

import com.innowise.userservice.dto.ChangeActiveStatusRequest;
import com.innowise.userservice.dto.CreateUserRequest;
import com.innowise.userservice.dto.UpdateUserRequest;
import com.innowise.userservice.dto.UserResponse;
import com.innowise.userservice.entity.User;
import com.innowise.userservice.service.UserService;
import com.innowise.userservice.specification.UserSpecification;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UserController {

  private final UserService service;

  @PostMapping
  public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
    UserResponse response = service.createUser(request);

    return ResponseEntity.status(201).body(response);
  }

  @GetMapping("/{id}")
  public ResponseEntity<UserResponse> getUserById(@PathVariable long id) {
    UserResponse response = service.getUserById(id);

    return ResponseEntity.ok(response);
  }

  @GetMapping
  public ResponseEntity<Page<UserResponse>> getAllUsers(
      @RequestParam(required = false) String name,
      @RequestParam(required = false) String surname,
      Pageable pageable) {

    Specification<User> userSpecification = Specification.allOf(UserSpecification.hasName(name),
        UserSpecification.hasSurname(surname));

    return ResponseEntity.ok(service.getAllUsers(userSpecification, pageable));
  }

  @PutMapping("/{id}")
  public ResponseEntity<UserResponse> updateUser(
      @PathVariable long id,
      @Valid @RequestBody UpdateUserRequest request) {

    UserResponse response = service.updateUser(id, request);

    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteUser(@PathVariable long id) {
    service.deleteUser(id);

    return ResponseEntity.noContent().build();
  }

  @GetMapping("/search/name")
  public ResponseEntity<Page<UserResponse>> getUsersByName(
      @RequestParam String name,
      Pageable pageable) {

    return ResponseEntity.ok(service.getAllUsersByName(name, pageable));
  }

  @GetMapping("/search/surname")
  public ResponseEntity<Page<UserResponse>> getUsersBySurname(
      @RequestParam String surname,
      Pageable pageable) {

    return ResponseEntity.ok(service.getAllUsersBySurname(surname, pageable));
  }

  @GetMapping("/active")
  public ResponseEntity<Page<UserResponse>> getActiveUsers(Pageable pageable) {
    return ResponseEntity.ok(service.getActiveUsers(pageable));
  }

  @GetMapping("/email")
  public ResponseEntity<UserResponse> getUserByEmail(@RequestParam String email) {
    UserResponse response = service.getUserByEmail(email);

    return ResponseEntity.ok(response);
  }

  @PatchMapping("/{id}/active")
  public ResponseEntity<UserResponse> changeActiveStatus(
      @PathVariable long id,
      @Valid @RequestBody ChangeActiveStatusRequest request) {

    UserResponse response = service.changeActiveStatus(id, request.getActive());

    return ResponseEntity.ok(response);
  }
}
