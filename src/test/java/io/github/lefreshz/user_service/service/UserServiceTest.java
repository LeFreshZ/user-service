package io.github.lefreshz.user_service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.lefreshz.user_service.dto.CreateUserRequest;
import io.github.lefreshz.user_service.dto.UpdateUserRequest;
import io.github.lefreshz.user_service.dto.UserResponse;
import io.github.lefreshz.user_service.entity.User;
import io.github.lefreshz.user_service.exception.UserAlreadyExistsException;
import io.github.lefreshz.user_service.exception.UserNotFoundException;
import io.github.lefreshz.user_service.mapper.UserMapper;
import io.github.lefreshz.user_service.repository.UserRepository;
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
public class UserServiceTest {

  @Mock
  private UserRepository repository;

  private UserService service;

  private User user;

  @BeforeEach
  void setup() {
    UserMapper mapper = Mappers.getMapper(UserMapper.class);

    service = new UserService(mapper, repository);

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
    when(repository.findByIdWithCards(1L)).thenReturn(Optional.of(user));

    UserResponse serviceResponse = service.getUserById(1L);

    assertEquals(1L, serviceResponse.getUserId());
    assertEquals("Andrey", serviceResponse.getName());
  }

  @Test
  void shouldCorrectlyAddUser() {
    CreateUserRequest createRequest = new CreateUserRequest();
    createRequest.setName("Andrey");
    createRequest.setEmail("thescaver@gmail.com");

    when(repository.save(any(User.class))).thenReturn(user);
    when(repository.existsByEmail("thescaver@gmail.com")).thenReturn(false);

    UserResponse serviceResponse = service.createUser(createRequest);

    assertEquals(1L, serviceResponse.getUserId());
    assertEquals("Andrey", serviceResponse.getName());
    assertEquals("thescaver@gmail.com", serviceResponse.getEmail());
    verify(repository).save(any(User.class));
  }

  @Test
  void shouldCorrectlyUpdateUser() {
    UpdateUserRequest updateRequest = new UpdateUserRequest();
    updateRequest.setName("Andr");
    updateRequest.setSurname("Gup");

    when(repository.findByIdWithCards(1L)).thenReturn(Optional.of(user));
    when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

    UserResponse response = service.updateUser(1L, updateRequest);

    assertEquals("Andr", response.getName());
    assertEquals("Gup", response.getSurname());
  }

  @Test
  void shouldCorrectlyDeleteUser() {
    when(repository.findByIdWithCards(1L)).thenReturn(Optional.of(user));

    service.deleteUser(1L);

    verify(repository).delete(user);
  }

  @Test
  void shouldChangeActiveStatus() {
    when(repository.findByIdWithCards(1L)).thenReturn(Optional.of(user));
    when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

    UserResponse response = service.changeActiveStatus(1L);

    assertEquals(false, response.getActive());
  }

  @Test
  void shouldThrowIfNoUserWithIdFound() {
    when(repository.findByIdWithCards(2L)).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> service.getUserById(2L));
  }

  @Test
  void shouldThrowIfEmailExists() {
    CreateUserRequest createRequest = new CreateUserRequest();
    createRequest.setName("Andrey");
    createRequest.setEmail("thescaver@gmail.com");

    when(repository.existsByEmail("thescaver@gmail.com")).thenReturn(true);

    assertThrows(UserAlreadyExistsException.class, () -> service.createUser(createRequest));
  }

  @Test
  void shouldReturnUserByEmail() {
    when(repository.findByEmail("thescaver@gmail.com")).thenReturn(Optional.of(user));

    UserResponse response = service.getUserByEmail("thescaver@gmail.com");

    assertEquals(1L, response.getUserId());
    assertEquals("Andrey", response.getName());
    assertEquals("thescaver@gmail.com", response.getEmail());
  }

  @Test
  void shouldThrowWhenEmailNotFound() {
    when(repository.findByEmail("test@gmail.com")).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> service.getUserByEmail("test@gmail.com"));
  }

  @Test
  void shouldReturnAllUsersBySpecification() {
    PageRequest pageable = PageRequest.of(0, 10);
    Specification<User> specification = (root, query, cb) -> null;
    Page<User> users = new PageImpl<>(List.of(user));

    when(repository.findAll(specification, pageable)).thenReturn(users);

    Page<UserResponse> response = service.getAllUsers(specification, pageable);

    assertEquals(1, response.getTotalElements());
    assertEquals("Andrey", response.getContent().get(0).getName());
  }

  @Test
  void shouldReturnActiveUsers() {
    PageRequest pageable = PageRequest.of(0, 10);
    Page<User> users = new PageImpl<>(List.of(user));

    when(repository.findByActiveTrue(pageable)).thenReturn(users);

    Page<UserResponse> response = service.getActiveUsers(pageable);

    assertEquals(1, response.getTotalElements());
    assertEquals(true, response.getContent().get(0).getActive());
  }

  @Test
  void shouldReturnUsersByName() {
    PageRequest pageable = PageRequest.of(0, 10);
    Page<User> users = new PageImpl<>(List.of(user));

    when(repository.searchByName("Andrey", pageable)).thenReturn(users);

    Page<UserResponse> response = service.getAllUsersByName("Andrey", pageable);

    assertEquals(1, response.getTotalElements());
    assertEquals("Andrey", response.getContent().get(0).getName());
  }

  @Test
  void shouldReturnUsersBySurname() {
    PageRequest pageable = PageRequest.of(0, 10);
    Page<User> users = new PageImpl<>(List.of(user));

    when(repository.searchBySurnameNative("Gupanov", pageable)).thenReturn(users);

    Page<UserResponse> response = service.getAllUsersBySurname("Gupanov", pageable);

    assertEquals(1, response.getTotalElements());
    assertEquals("Gupanov", response.getContent().get(0).getSurname());
  }
}
