package com.innowise.userservice.dao;

import com.innowise.userservice.entity.User;
import com.innowise.userservice.repository.UserRepository;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class UserDao {

  private final UserRepository repository;

  public User save(User user) {
    return repository.save(user);
  }

  public Optional<User> findById(Long id) {
    return repository.findById(id);
  }

  public boolean existsById(Long id) {
    return repository.existsById(id);
  }

  public boolean existsByEmail(String email) {
    return repository.existsByEmail(email);
  }

  public Page<User> findAll(Specification<User> specification, Pageable pageable) {
    return repository.findAll(specification, pageable);
  }

  public Optional<User> findByEmail(String email) {
    return repository.findByEmail(email);
  }

  public Page<User> findActive(Pageable pageable) {
    return repository.findByActiveTrue(pageable);
  }

  public Page<User> searchByName(String name, Pageable pageable) {
    return repository.searchByName(name, pageable);
  }

  public Page<User> searchBySurname(String surname, Pageable pageable) {
    return repository.searchBySurnameNative(surname, pageable);
  }

  public void delete(User user) {
    repository.delete(user);
  }

  public Optional<User> findByIdWithCards(Long id) {
    return repository.findByIdWithCards(id);
  }
}
