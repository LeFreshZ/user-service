package com.innowise.userservice.service;

import com.innowise.userservice.dto.CreateUserRequest;
import com.innowise.userservice.dto.UpdateUserRequest;
import com.innowise.userservice.dto.UserResponse;
import com.innowise.userservice.entity.User;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service interface for managing users.
 *
 * <p>Provides operations for creating, retrieving, updating, and deleting users,
 * as well as managing their active status. Frequently accessed users are cached in the
 * {@code "users"} cache to reduce database load.
 */
public interface UserService {

  /**
   * Creates a new user with the provided details.
   *
   * @param request the request object containing user registration data
   * @return the created user as a {@link UserResponse}
   * @throws com.innowise.userservice.exception.UserAlreadyExistsException if a user with the given
   *                                                                       email already exists
   */
  UserResponse createUser(CreateUserRequest request);

  /**
   * Retrieves a user along with their payment cards by unique identifier.
   *
   * <p>The result is cached in the {@code "users"} cache under the given {@code id}
   * to avoid repeated database lookups for the same user.
   *
   * @param id the unique identifier of the user
   * @return the found user as a {@link UserResponse}
   * @throws com.innowise.userservice.exception.UserNotFoundException if no user with the given ID
   *                                                                  exists
   */
  @Cacheable(value = "users", key = "#id")
  UserResponse getUserById(long id);

  /**
   * Retrieves a paginated list of users matching the given specification.
   *
   * @param specification the JPA specification used to filter users
   * @param pageable      pagination and sorting parameters
   * @return a {@link Page} of {@link UserResponse} objects matching the specification
   */
  Page<UserResponse> getAllUsers(Specification<User> specification, Pageable pageable);

  /**
   * Retrieves a user by their email address.
   *
   * @param email the email address of the user
   * @return the found user as a {@link UserResponse}
   * @throws com.innowise.userservice.exception.UserNotFoundException if no user with the given
   *                                                                  email exists
   */
  UserResponse getUserByEmail(String email);

  /**
   * Retrieves a paginated list of all active users.
   *
   * @param pageable pagination and sorting parameters
   * @return a {@link Page} of active {@link UserResponse} objects
   */
  Page<UserResponse> getActiveUsers(Pageable pageable);

  /**
   * Retrieves a paginated list of users filtered by first name.
   *
   * @param name     the full or partial first name to search by
   * @param pageable pagination and sorting parameters
   * @return a {@link Page} of {@link UserResponse} objects matching the given name
   */
  Page<UserResponse> getAllUsersByName(String name, Pageable pageable);

  /**
   * Retrieves a paginated list of users filtered by surname.
   *
   * @param surname  the full or partial surname to search by
   * @param pageable pagination and sorting parameters
   * @return a {@link Page} of {@link UserResponse} objects matching the given surname
   */
  Page<UserResponse> getAllUsersBySurname(String surname, Pageable pageable);

  /**
   * Updates an existing user with the provided data.
   *
   * <p>The {@code "users"} cache entry for the given {@code id} is updated
   * with the returned value after a successful update.
   *
   * @param id      the unique identifier of the user to update
   * @param request the request object containing updated user fields
   * @return the updated user as a {@link UserResponse}
   * @throws com.innowise.userservice.exception.UserNotFoundException      if no user with the given
   *                                                                       ID exists
   * @throws com.innowise.userservice.exception.UserAlreadyExistsException if another user with the
   *                                                                       provided email already
   *                                                                       exists
   */
  @CachePut(value = "users", key = "#id")
  @Transactional
  UserResponse updateUser(long id, UpdateUserRequest request);

  /**
   * Deletes a user by their unique identifier.
   *
   * <p>The corresponding entry is evicted from the {@code "users"} cache
   * after the user is successfully deleted.
   *
   * @param id the unique identifier of the user to delete
   * @throws com.innowise.userservice.exception.UserNotFoundException if no user with the given ID
   *                                                                  exists
   */
  @CacheEvict(value = "users", key = "#id")
  @Transactional
  void deleteUser(long id);

  /**
   * Changes the active status of a user.
   *
   * <p>The {@code "users"} cache entry for the given {@code id} is updated
   * with the returned value after the status change.
   *
   * @param id     the unique identifier of the user
   * @param active {@code true} to activate the user, {@code false} to deactivate them
   * @return the updated user as a {@link UserResponse}
   * @throws com.innowise.userservice.exception.UserNotFoundException if no user with the given ID
   *                                                                  exists
   */
  @CachePut(value = "users", key = "#id")
  @Transactional
  UserResponse changeActiveStatus(long id, boolean active);
}
