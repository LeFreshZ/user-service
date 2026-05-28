package com.innowise.userservice.integration;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.userservice.dto.ChangeActiveStatusRequest;
import com.innowise.userservice.dto.CreatePaymentCardRequest;
import com.innowise.userservice.dto.CreateUserRequest;
import com.innowise.userservice.dto.UpdatePaymentCardRequest;
import com.innowise.userservice.dto.UpdateUserRequest;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public abstract class IntegrationTest {

  @DynamicPropertySource
  static void setProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", TestContainersConfig.POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", TestContainersConfig.POSTGRES::getUsername);
    registry.add("spring.datasource.password", TestContainersConfig.POSTGRES::getPassword);

    registry.add("spring.data.redis.host", TestContainersConfig.REDIS::getHost);
    registry.add("spring.data.redis.port", () -> TestContainersConfig.REDIS.getMappedPort(6379));

    registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
  }

  protected MockMvc mvc;

  @Autowired
  private WebApplicationContext context;

  @Autowired
  protected JdbcTemplate jdbcTemplate;

  @Autowired
  protected StringRedisTemplate redisTemplate;

  @Autowired
  protected ObjectMapper mapper;

  @BeforeEach
  void clean() {
    mvc = MockMvcBuilders.webAppContextSetup(context)
        .apply(springSecurity())
            .defaultRequest(get("/")
                .header("X-User-Id", "1")
                .header("X-User-Role", "ROLE_ADMIN"))
                .build();

    jdbcTemplate.execute("TRUNCATE TABLE payment_cards, users RESTART IDENTITY CASCADE");
    redisTemplate.delete(redisTemplate.keys("*"));
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

  protected String updateCardRequest(
      String holder,
      LocalDate expirationDate) throws JsonProcessingException {

    UpdatePaymentCardRequest request = new UpdatePaymentCardRequest();

    request.setHolder(holder);
    request.setExpirationDate(expirationDate);

    return mapper.writeValueAsString(request);
  }

  protected String changeStatusRequest(Boolean active) throws JsonProcessingException {
    ChangeActiveStatusRequest request = new ChangeActiveStatusRequest();

    request.setActive(active);

    return mapper.writeValueAsString(request);
  }
}
