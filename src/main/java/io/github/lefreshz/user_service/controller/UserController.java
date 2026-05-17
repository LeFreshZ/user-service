package io.github.lefreshz.user_service.controller;

import io.github.lefreshz.user_service.dto.CreateUserRequest;
import io.github.lefreshz.user_service.dto.UpdateUserRequest;
import io.github.lefreshz.user_service.dto.UserResponse;
import io.github.lefreshz.user_service.entity.User;
import io.github.lefreshz.user_service.exception.UserAlreadyExistsException;
import io.github.lefreshz.user_service.exception.UserNotFoundException;
import io.github.lefreshz.user_service.service.UserService;
import io.github.lefreshz.user_service.specification.UserSpecification;
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
    UserResponse response;

    try {
      response = service.createUser(request);
    } catch (UserAlreadyExistsException ex) {
      return ResponseEntity.status(409).build();
    }

    return ResponseEntity.status(201).body(response);
  }

  @GetMapping("/{id}")
  public ResponseEntity<UserResponse> getUserById(@PathVariable long id) {
    UserResponse response;

    try {
      response = service.getUserById(id);
    } catch (UserNotFoundException ex) {
      return ResponseEntity.notFound().build();
    }

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

    UserResponse response;

    try {
      response = service.updateUser(id, request);
    } catch (UserNotFoundException ex) {
      return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteUser(@PathVariable long id) {
    try {
      service.deleteUser(id);
    } catch (UserNotFoundException ex) {
      return ResponseEntity.notFound().build();
    }

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
    UserResponse response;

    try {
      response = service.getUserByEmail(email);
    } catch (UserNotFoundException ex) {
      return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok(response);
  }

  @PatchMapping("/{id}/active")
  public ResponseEntity<UserResponse> changeActiveStatus(@PathVariable long id) {
    UserResponse response;

    try {
      response = service.changeActiveStatus(id);
    } catch (UserNotFoundException ex) {
      return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok(response);
  }
}
