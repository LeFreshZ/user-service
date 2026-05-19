package io.github.lefreshz.user_service.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.lefreshz.user_service.dto.CreatePaymentCardRequest;
import io.github.lefreshz.user_service.dto.CreateUserRequest;
import io.github.lefreshz.user_service.dto.UpdatePaymentCardRequest;
import io.github.lefreshz.user_service.dto.UpdateUserRequest;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public abstract class IntegrationTest {

  @Container
  static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>(
      "postgres:16-alpine")
      .withDatabaseName("user_db")
      .withUsername("user")
      .withPassword("password");

  @Container
  static GenericContainer<?> redisContainer = new GenericContainer<>("redis:7.4-alpine")
      .withExposedPorts(6379);

  @DynamicPropertySource
  static void setProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
    registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
    registry.add("spring.datasource.password", postgreSQLContainer::getPassword);

    registry.add("spring.data.redis.host", redisContainer::getHost);
    registry.add("spring.data.redis.port", () -> redisContainer.getMappedPort(6379));

    registry.add("spring.cache.type", () -> "redis");
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
  }

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Autowired
  protected StringRedisTemplate redisTemplate;

  @Autowired
  protected MockMvc mvc;

  @Autowired
  protected ObjectMapper mapper;

  @BeforeEach
  void clean() {
    jdbcTemplate.execute("TRUNCATE TABLE payment_cards, users RESTART IDENTITY CASCADE");
    redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
  }

  protected String createUserRequest(
      String name,
      String surname,
      String email,
      LocalDate birthDate) throws JsonProcessingException {

    CreateUserRequest request = new CreateUserRequest();

    request.setName(name);
    request.setSurname(surname);
    request.setBirthDate(birthDate);
    request.setEmail(email);

    return mapper.writeValueAsString(request);
  }

  protected String createCardRequest(
      String number,
      String holder,
      Long userId,
      LocalDate expirationDate) throws JsonProcessingException {

    CreatePaymentCardRequest request = new CreatePaymentCardRequest();

    request.setNumber(number);
    request.setHolder(holder);
    request.setExpirationDate(expirationDate);
    request.setUserId(userId);

    return mapper.writeValueAsString(request);
  }

  protected String updateUserRequest(
      String name,
      String surname,
      String email,
      LocalDate birthDate) throws JsonProcessingException {

    UpdateUserRequest request = new UpdateUserRequest();

    request.setName(name);
    request.setSurname(surname);
    request.setBirthDate(birthDate);
    request.setEmail(email);

    return mapper.writeValueAsString(request);
  }
}
