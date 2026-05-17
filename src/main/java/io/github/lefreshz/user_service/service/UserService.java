package io.github.lefreshz.user_service.service;

import io.github.lefreshz.user_service.dto.CreateUserRequest;
import io.github.lefreshz.user_service.dto.PaymentCardResponse;
import io.github.lefreshz.user_service.dto.UpdateUserRequest;
import io.github.lefreshz.user_service.dto.UserResponse;
import io.github.lefreshz.user_service.entity.PaymentCard;
import io.github.lefreshz.user_service.entity.User;
import io.github.lefreshz.user_service.exception.UserAlreadyExistsException;
import io.github.lefreshz.user_service.exception.UserNotFoundException;
import io.github.lefreshz.user_service.mapper.UserMapper;
import io.github.lefreshz.user_service.repository.UserRepository;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class UserService {

  private final UserMapper mapper;
  private final UserRepository repository;

  public UserResponse createUser(CreateUserRequest request) {
    if (repository.existsByEmail(request.getEmail())) {
      throw new UserAlreadyExistsException("User already exists with email: " + request.getEmail());
    }

    User user = mapper.toEntity(request);
    user.setActive(true);

    User savedUser = repository.save(user);

    return mapper.toResponse(savedUser);
  }

  public UserResponse getUserById(long id) {
    return mapper.toResponse(getUser(id));
  }

  public Page<UserResponse> getAllUsers(Specification<User> specification, Pageable pageable) {
    Page<User> userPage = repository.findAll(specification, pageable);

    return userPage.map(mapper::toResponse);
  }

  public UserResponse getUserByEmail(String email) {
    Optional<User> optionalUser = repository.findByEmail(email);

    if (optionalUser.isEmpty()) {
      throw new UserNotFoundException("Can not find user with email: " + email);
    }

    User user = optionalUser.get();

    return mapper.toResponse(user);
  }

  public Page<UserResponse> getActiveUsers(Pageable pageable) {
    return repository.findByActiveTrue(pageable).map(mapper::toResponse);
  }

  public Page<UserResponse> getAllUsersByName(String name, Pageable pageable) {
    return repository.searchByName(name, pageable).map(mapper::toResponse);
  }

  public Page<UserResponse> getAllUsersBySurname(String surname, Pageable pageable) {
    return repository.searchBySurnameNative(surname, pageable).map(mapper::toResponse);
  }

  @Transactional
  public UserResponse updateUser(long id, UpdateUserRequest request) {
    User user = getUser(id);

    mapper.updateUser(request, user);

    User savedUser = repository.save(user);

    return mapper.toResponse(savedUser);
  }

  @Transactional
  public void deleteUser(long id) {
    User user = getUser(id);

    repository.delete(user);
  }

  @Transactional
  public UserResponse changeActiveStatus(long id) {
    User user = getUser(id);

    user.setActive(!user.getActive());

    User savedUser = repository.save(user);

    return mapper.toResponse(savedUser);
  }

  private User getUser(long id) {
    Optional<User> optionalUser = repository.findById(id);

    if (optionalUser.isEmpty()) {
      throw new UserNotFoundException("Can not find user with userId: " + id);
    }

    return optionalUser.get();
  }
}
