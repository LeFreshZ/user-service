package io.github.lefreshz.user_service.integration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.lefreshz.user_service.dto.PaymentCardResponse;
import io.github.lefreshz.user_service.dto.UserResponse;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

public class PaymentCardIntegrationTest extends IntegrationTest {

  @Test
  void shouldCreateCardAndEvictUser() throws Exception {
    String userRequest = createUserRequest("Andrey",
        "Gupanov",
        "test@gmail.com",
        LocalDate.of(2006, 2, 1));

    MvcResult userResult =
        mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(userRequest))
            .andExpect(status().isCreated())
            .andReturn();

    UserResponse userResponse = mapper.readValue(userResult.getResponse().getContentAsString(),
        UserResponse.class);

    mvc.perform(get("/users/{id}", userResponse.getUserId()))
        .andExpect(status().isOk());

    assertTrue(redisTemplate.hasKey("users::" + userResponse.getUserId()));

    String cardRequest = createCardRequest("1234432112344321",
        "Andrey Gupanov",
        userResponse.getUserId(),
        LocalDate.of(2030, 2, 1));

    mvc.perform(post("/payment-cards").contentType(MediaType.APPLICATION_JSON).content(cardRequest))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.number").value("1234432112344321"));

    assertFalse(redisTemplate.hasKey("users::" + userResponse.getUserId()));

    mvc.perform(get("/users/{id}", userResponse.getUserId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.paymentCards[0].number").value("1234432112344321"));
  }

  @Test
  void shouldDeleteCardAndEvictUser() throws Exception {
    String userRequest = createUserRequest("Andrey",
        "Gupanov",
        "test@gmail.com",
        LocalDate.of(2006, 2, 1));

    MvcResult userResult =
        mvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userRequest))
            .andExpect(status().isCreated())
            .andReturn();

    UserResponse userResponse = mapper.readValue(userResult.getResponse().getContentAsString(),
        UserResponse.class);

    String cardRequest = createCardRequest("1234432112344321",
        "Andrey Gupanov",
        userResponse.getUserId(),
        LocalDate.of(2030, 2, 1));

    MvcResult cardResult =
        mvc.perform(post("/payment-cards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(cardRequest))
            .andExpect(status().isCreated())
            .andReturn();

    PaymentCardResponse cardResponse = mapper.readValue(
        cardResult.getResponse().getContentAsString(),
        PaymentCardResponse.class);

    mvc.perform(get("/users/{id}", userResponse.getUserId()))
        .andExpect(status().isOk());

    assertTrue(redisTemplate.hasKey("users::" + userResponse.getUserId()));

    mvc.perform(delete("/payment-cards/{cardId}", cardResponse.getCardId()))
        .andExpect(status().isNoContent());

    assertFalse(redisTemplate.hasKey("users::" + userResponse.getUserId()));

    mvc.perform(get("/users/{id}", userResponse.getUserId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.paymentCards").isEmpty());
  }

  @Test
  void shouldChangeActiveStatusAndEvictUser() throws Exception {
    String userRequest = createUserRequest("Andrey",
        "Gupanov",
        "test@gmail.com",
        LocalDate.of(2006, 2, 1));

    MvcResult userResult =
        mvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userRequest))
            .andExpect(status().isCreated())
            .andReturn();

    UserResponse userResponse = mapper.readValue(userResult.getResponse().getContentAsString(),
        UserResponse.class);

    String cardRequest = createCardRequest("1234432112344321",
        "Andrey Gupanov",
        userResponse.getUserId(),
        LocalDate.of(2030, 2, 1));

    MvcResult cardResult =
        mvc.perform(post("/payment-cards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(cardRequest))
            .andExpect(status().isCreated())
            .andReturn();

    PaymentCardResponse cardResponse = mapper.readValue(
        cardResult.getResponse().getContentAsString(),
        PaymentCardResponse.class);

    mvc.perform(get("/users/{id}", userResponse.getUserId()))
        .andExpect(status().isOk());

    assertTrue(redisTemplate.hasKey("users::" + userResponse.getUserId()));

    mvc.perform(patch("/payment-cards/{id}/active", cardResponse.getCardId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.active").value(false));

    assertFalse(redisTemplate.hasKey("users::" + userResponse.getUserId()));
  }
}
