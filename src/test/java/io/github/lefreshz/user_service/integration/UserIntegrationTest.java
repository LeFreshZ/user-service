package io.github.lefreshz.user_service.integration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.lefreshz.user_service.dto.UserResponse;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

public class UserIntegrationTest extends IntegrationTest {

  @Test
  void shouldCreateAndCacheUserThenReturnFromDb() throws Exception {
    String request = createUserRequest(
        "Andrey",
        "Gupanov",
        "test@gmail.com",
        LocalDate.of(2006, 2, 1));

    MvcResult result =
        mvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.userId").exists())
            .andExpect(jsonPath("$.name").value("Andrey"))
            .andReturn();

    UserResponse response = mapper.readValue(result.getResponse().getContentAsString(),
        UserResponse.class);

    mvc.perform(get("/users/{id}", response.getUserId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("test@gmail.com"));

    assertTrue(redisTemplate.hasKey("users::" + response.getUserId()));
  }

  @Test
  void shouldUpdateAndRefreshCache() throws Exception {
    String createRequest = createUserRequest(
        "Andrey",
        "Gupanov",
        "test@gmail.com",
        LocalDate.of(2006, 2, 1));

    MvcResult result =
        mvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRequest))
            .andExpect(status().isCreated())
            .andReturn();

    UserResponse response = mapper.readValue(result.getResponse().getContentAsString(),
        UserResponse.class);

    mvc.perform(get("/users/{id}", response.getUserId()))
        .andExpect(status().isOk());

    String updateRequest = updateUserRequest(
        "Andr",
        "Gupanov",
        "andrey@gmail.com",
        LocalDate.of(2006, 2, 1));

    mvc.perform(put("/users/{id}", response.getUserId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(updateRequest))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Andr"))
        .andExpect(jsonPath("$.email").value("andrey@gmail.com"));

    mvc.perform(get("/users/{id}", response.getUserId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Andr"));

    String cachedUser = redisTemplate.opsForValue()
        .get("users::" + response.getUserId());

    assertTrue(cachedUser.contains("Andr"));
    assertTrue(cachedUser.contains("andrey@gmail.com"));
  }

  @Test
  void shouldDeleteUserAndEvictCache() throws Exception {
    String request = createUserRequest(
        "Andrey",
        "Gupanov",
        "test@gmail.com",
        LocalDate.of(2006, 2, 1));

    MvcResult result =
        mvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isCreated())
            .andReturn();

    UserResponse response = mapper.readValue(result.getResponse().getContentAsString(),
        UserResponse.class);

    mvc.perform(get("/users/{id}", response.getUserId()))
        .andExpect(status().isOk());

    assertTrue(redisTemplate.hasKey("users::" + response.getUserId()));

    mvc.perform(delete("/users/{id}", response.getUserId()))
        .andExpect(status().isNoContent());

    mvc.perform(get("/users/{id}", response.getUserId()))
        .andExpect(status().isNotFound());

    assertFalse(redisTemplate.hasKey("users::" + response.getUserId()));
  }

  @Test
  void shouldReturnUserByEmail() throws Exception {
    String request = createUserRequest(
        "Andrey",
        "Gupanov",
        "test@gmail.com",
        LocalDate.of(2006, 2, 1));

    mvc.perform(post("/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isCreated());

    mvc.perform(get("/users/email")
            .param("email", "test@gmail.com"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("test@gmail.com"))
        .andExpect(jsonPath("$.name").value("Andrey"));
  }

  @Test
  void shouldReturnActiveUsers() throws Exception {
    String request = createUserRequest(
        "Andrey",
        "Gupanov",
        "test@gmail.com",
        LocalDate.of(2006, 2, 1));

    mvc.perform(post("/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isCreated());

    mvc.perform(get("/users/active"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].active").value(true));
  }

  @Test
  void shouldReturnAllUsersBySpecification() throws Exception {
    String request = createUserRequest(
        "Andrey",
        "Gupanov",
        "test@gmail.com",
        LocalDate.of(2006, 2, 1));

    mvc.perform(post("/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
        .andExpect(status().isCreated());

    mvc.perform(get("/users")
            .param("name", "Andrey"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].name").value("Andrey"));
  }

  @Test
  void shouldChangeActiveStatus() throws Exception {
    String request = createUserRequest("Andrey",
        "Gupanov",
        "test@gmail.com",
        LocalDate.of(2006, 2, 1));

    MvcResult result =
        mvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isCreated())
            .andReturn();

    UserResponse response = mapper.readValue(result.getResponse().getContentAsString(),
        UserResponse.class);

    mvc.perform(patch("/users/{id}/active", response.getUserId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.active").value(false));
  }

  @Test
  void shouldBe404IfUserNotFound() throws Exception {
    mvc.perform(get("/users/{id}", 50))
        .andExpect(status().isNotFound());
  }
}
