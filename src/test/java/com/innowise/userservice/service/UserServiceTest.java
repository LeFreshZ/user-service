package com.innowise.userservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.innowise.userservice.dao.UserDao;
import com.innowise.userservice.dto.CreateUserRequest;
import com.innowise.userservice.dto.UpdateUserRequest;
import com.innowise.userservice.dto.UserResponse;
import com.innowise.userservice.entity.User;
import com.innowise.userservice.exception.UserAlreadyExistsException;
import com.innowise.userservice.exception.UserNotFoundException;
import com.innowise.userservice.mapper.UserMapper;
import com.innowise.userservice.service.implementation.UserServiceImpl;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock
  private UserDao dao;

  private UserService service;

  private User user;

  @BeforeEach
  void setup() {
    UserMapper mapper = Mappers.getMapper(UserMapper.class);

    service = new UserServiceImpl(mapper, dao);

    user = new User();
    user.setUserId(1L);
    user.setName("Andrey");
    user.setSurname("Gupanov");
    user.setEmail("thescaver@gmail.com");
    user.setBirthDate(LocalDate.of(2006, 2, 1));
    user.setActive(true);
    user.setPaymentCards(null);
  }

  @Test
  void shouldReturnUserById() {
    when(dao.findByIdWithCards(1L)).thenReturn(Optional.of(user));

    UserResponse serviceResponse = service.getUserById(1L);

    assertEquals(1L, serviceResponse.getUserId());
    assertEquals("Andrey", serviceResponse.getName());
  }

  @Test
  void shouldCorrectlyAddUser() {
    CreateUserRequest createRequest = new CreateUserRequest();
    createRequest.setName("Andrey");
    createRequest.setEmail("thescaver@gmail.com");

    when(dao.save(any(User.class))).thenReturn(user);
    when(dao.existsByEmail("thescaver@gmail.com")).thenReturn(false);

    UserResponse serviceResponse = service.createUser(createRequest);

    assertEquals(1L, serviceResponse.getUserId());
    assertEquals("Andrey", serviceResponse.getName());
    assertEquals("thescaver@gmail.com", serviceResponse.getEmail());
    verify(dao).save(any(User.class));
  }

  @Test
  void shouldCorrectlyUpdateUser() {
    UpdateUserRequest updateRequest = new UpdateUserRequest();
    updateRequest.setName("Andr");
    updateRequest.setSurname("Gup");

    when(dao.findByIdWithCards(1L)).thenReturn(Optional.of(user));
    when(dao.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

    UserResponse response = service.updateUser(1L, updateRequest);

    assertEquals("Andr", response.getName());
    assertEquals("Gup", response.getSurname());
  }

  @Test
  void shouldCorrectlyDeleteUser() {
    when(dao.findByIdWithCards(1L)).thenReturn(Optional.of(user));

    service.deleteUser(1L);

    verify(dao).delete(user);
  }

  @Test
  void shouldChangeActiveStatus() {
    when(dao.findByIdWithCards(1L)).thenReturn(Optional.of(user));
    when(dao.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

    UserResponse response = service.changeActiveStatus(1L, false);

    assertEquals(false, response.getActive());
  }

  @Test
  void shouldThrowIfNoUserWithIdFound() {
    when(dao.findByIdWithCards(2L)).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> service.getUserById(2L));
  }

  @Test
  void shouldThrowIfEmailExists() {
    CreateUserRequest createRequest = new CreateUserRequest();
    createRequest.setName("Andrey");
    createRequest.setEmail("thescaver@gmail.com");

    when(dao.existsByEmail("thescaver@gmail.com")).thenReturn(true);

    assertThrows(UserAlreadyExistsException.class, () -> service.createUser(createRequest));
  }

  @Test
  void shouldReturnUserByEmail() {
    when(dao.findByEmail("thescaver@gmail.com")).thenReturn(Optional.of(user));

    UserResponse response = service.getUserByEmail("thescaver@gmail.com");

    assertEquals(1L, response.getUserId());
    assertEquals("Andrey", response.getName());
    assertEquals("thescaver@gmail.com", response.getEmail());
  }

  @Test
  void shouldThrowWhenEmailNotFound() {
    when(dao.findByEmail("test@gmail.com")).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> service.getUserByEmail("test@gmail.com"));
  }

  @Test
  void shouldReturnAllUsersBySpecification() {
    PageRequest pageable = PageRequest.of(0, 10);
    Specification<User> specification = (root, query, cb) -> null;
    Page<User> users = new PageImpl<>(List.of(user));

    when(dao.findAll(specification, pageable)).thenReturn(users);

    Page<UserResponse> response = service.getAllUsers(specification, pageable);

    assertEquals(1, response.getTotalElements());
    assertEquals("Andrey", response.getContent().get(0).getName());
  }

  @Test
  void shouldReturnActiveUsers() {
    PageRequest pageable = PageRequest.of(0, 10);
    Page<User> users = new PageImpl<>(List.of(user));

    when(dao.findActive(pageable)).thenReturn(users);

    Page<UserResponse> response = service.getActiveUsers(pageable);

    assertEquals(1, response.getTotalElements());
    assertEquals(true, response.getContent().get(0).getActive());
  }

  @Test
  void shouldReturnUsersByName() {
    PageRequest pageable = PageRequest.of(0, 10);
    Page<User> users = new PageImpl<>(List.of(user));

    when(dao.searchByName("Andrey", pageable)).thenReturn(users);

    Page<UserResponse> response = service.getAllUsersByName("Andrey", pageable);

    assertEquals(1, response.getTotalElements());
    assertEquals("Andrey", response.getContent().get(0).getName());
  }

  @Test
  void shouldReturnUsersBySurname() {
    PageRequest pageable = PageRequest.of(0, 10);
    Page<User> users = new PageImpl<>(List.of(user));

    when(dao.searchBySurname("Gupanov", pageable)).thenReturn(users);

    Page<UserResponse> response = service.getAllUsersBySurname("Gupanov", pageable);

    assertEquals(1, response.getTotalElements());
    assertEquals("Gupanov", response.getContent().get(0).getSurname());
  }
}
