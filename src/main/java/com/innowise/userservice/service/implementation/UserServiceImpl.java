package com.innowise.userservice.service.implementation;

import com.innowise.userservice.dao.UserDao;
import com.innowise.userservice.dto.CreateUserRequest;
import com.innowise.userservice.dto.UpdateUserRequest;
import com.innowise.userservice.dto.UserResponse;
import com.innowise.userservice.entity.User;
import com.innowise.userservice.exception.UserAlreadyExistsException;
import com.innowise.userservice.exception.UserNotFoundException;
import com.innowise.userservice.mapper.UserMapper;
import com.innowise.userservice.service.UserService;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserMapper mapper;
  private final UserDao dao;

  @Override
  public UserResponse createUser(CreateUserRequest request) {
    if (dao.existsByEmail(request.getEmail())) {
      throw new UserAlreadyExistsException("User already exists with email: " + request.getEmail());
    }

    User user = mapper.toEntity(request);
    user.setActive(true);

    User savedUser = dao.save(user);

    return mapper.toResponse(savedUser);
  }

  @Cacheable(value = "users", key = "#id")
  @Override
  public UserResponse getUserById(long id) {
    return mapper.toResponse(getUser(id));
  }

  @Override
  public Page<UserResponse> getAllUsers(Specification<User> specification, Pageable pageable) {
    Page<User> userPage = dao.findAll(specification, pageable);

    return userPage.map(mapper::toResponse);
  }

  @Override
  public UserResponse getUserByEmail(String email) {
    Optional<User> optionalUser = dao.findByEmail(email);

    if (optionalUser.isEmpty()) {
      throw new UserNotFoundException("Can not find user with email: " + email);
    }

    User user = optionalUser.get();

    return mapper.toResponse(user);
  }

  @Override
  public Page<UserResponse> getActiveUsers(Pageable pageable) {
    return dao.findActive(pageable).map(mapper::toResponse);
  }

  @Override
  public Page<UserResponse> getAllUsersByName(String name, Pageable pageable) {
    return dao.searchByName(name, pageable).map(mapper::toResponse);
  }

  @Override
  public Page<UserResponse> getAllUsersBySurname(String surname, Pageable pageable) {
    return dao.searchBySurname(surname, pageable).map(mapper::toResponse);
  }

  @CachePut(value = "users", key = "#id")
  @Transactional
  @Override
  public UserResponse updateUser(long id, UpdateUserRequest request) {
    User user = getUser(id);

    if (!user.getEmail().equals(request.getEmail()) && dao.existsByEmail(request.getEmail())) {
      throw new UserAlreadyExistsException("User already exists with email: " + request.getEmail());
    }

    mapper.updateUser(request, user);

    User savedUser = dao.save(user);

    return mapper.toResponse(savedUser);
  }

  @CacheEvict(value = "users", key = "#id")
  @Transactional
  @Override
  public void deleteUser(long id) {
    User user = getUser(id);

    dao.delete(user);
  }

  @CachePut(value = "users", key = "#id")
  @Transactional
  @Override
  public UserResponse changeActiveStatus(long id, boolean active) {
    User user = getUser(id);

    user.setActive(active);

    User savedUser = dao.save(user);

    return mapper.toResponse(savedUser);
  }

  private User getUser(long id) {
    Optional<User> optionalUser = dao.findByIdWithCards(id);

    if (optionalUser.isEmpty()) {
      throw new UserNotFoundException("Can not find user with userId: " + id);
    }

    return optionalUser.get();
  }
}
