package io.github.lefreshz.user_service.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.lefreshz.user_service.dto.CreatePaymentCardRequest;
import io.github.lefreshz.user_service.dto.PaymentCardResponse;
import io.github.lefreshz.user_service.dto.UpdatePaymentCardRequest;
import io.github.lefreshz.user_service.entity.PaymentCard;
import io.github.lefreshz.user_service.entity.User;
import io.github.lefreshz.user_service.exception.CardLimitExceededException;
import io.github.lefreshz.user_service.exception.PaymentCardAlreadyExistsException;
import io.github.lefreshz.user_service.exception.PaymentCardNotFoundException;
import io.github.lefreshz.user_service.exception.UserNotFoundException;
import io.github.lefreshz.user_service.mapper.PaymentCardMapper;
import io.github.lefreshz.user_service.repository.PaymentCardRepository;
import io.github.lefreshz.user_service.repository.UserRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;

@ExtendWith(MockitoExtension.class)
public class PaymentCardServiceTest {

  @Mock
  private PaymentCardRepository paymentCardRepository;

  @Mock
  private CacheManager cacheManager;

  @Mock
  private UserRepository userRepository;

  private PaymentCardService service;

  private PaymentCard card;

  @BeforeEach
  void setup() {
    PaymentCardMapper mapper = Mappers.getMapper(PaymentCardMapper.class);

    service = new PaymentCardService(mapper, paymentCardRepository, userRepository, cacheManager);

    card = new PaymentCard();
    card.setCardId(1L);
    card.setNumber("1234432112344321");
    card.setHolder("Andrey");
    card.setExpirationDate(LocalDate.of(2030, 2, 1));
    card.setActive(true);
    User user = new User();
    user.setUserId(1L);
    card.setUser(user);
  }

  @Test
  void shouldReturnCardById() {
    when(paymentCardRepository.findById(1L)).thenReturn(Optional.of(card));

    PaymentCardResponse response = service.getCardById(1L);

    assertEquals("1234432112344321", response.getNumber());
    assertEquals("Andrey", response.getHolder());
  }

  @Test
  void shouldAddCard() {
    CreatePaymentCardRequest createRequest = createPaymentCardRequest();

    when(paymentCardRepository.existsByNumber("1234432112344321")).thenReturn(false);
    when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
    when(paymentCardRepository.countByUser_UserId(1L)).thenReturn(3);
    when(paymentCardRepository.save(any(PaymentCard.class))).thenReturn(card);

    PaymentCardResponse response = service.createCard(createRequest);

    assertEquals("1234432112344321", response.getNumber());
    assertEquals("Andrey", response.getHolder());
    assertEquals(LocalDate.of(2030, 2, 1), response.getExpirationDate());
    verify(paymentCardRepository).save(any(PaymentCard.class));
  }

  @Test
  void shouldCorrectlyUpdateCard() {
    UpdatePaymentCardRequest updateRequest = new UpdatePaymentCardRequest();
    updateRequest.setHolder("Kate");
    updateRequest.setExpirationDate(LocalDate.of(2035, 8, 2));

    when(paymentCardRepository.findById(1L)).thenReturn(Optional.of(card));
    when(paymentCardRepository.save(any(PaymentCard.class))).thenAnswer(
        invocation -> invocation.getArgument(0));

    PaymentCardResponse response = service.updateCard(1L, updateRequest);

    assertEquals("Kate", response.getHolder());
    assertEquals(LocalDate.of(2035, 8, 2), response.getExpirationDate());
  }

  @Test
  void shouldCorrectlyDeleteCard() {
    when(paymentCardRepository.findById(1L)).thenReturn(Optional.of(card));

    service.deleteCard(1L);

    verify(paymentCardRepository).delete(card);
  }

  @Test
  void shouldChangeActiveStatus() {
    when(paymentCardRepository.findById(1L)).thenReturn(Optional.of(card));
    when(paymentCardRepository.save(any(PaymentCard.class))).thenAnswer(
        invocation -> invocation.getArgument(0));

    PaymentCardResponse response = service.changeActiveStatus(1L);

    assertEquals(false, response.getActive());
  }

  @Test
  void shouldThrowIfNoUserFound() {
    CreatePaymentCardRequest createRequest = createPaymentCardRequest();

    when(paymentCardRepository.existsByNumber("1234432112344321")).thenReturn(false);
    when(userRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> service.createCard(createRequest));
  }

  @Test
  void shouldThrowIfUserHasFiveCards() {
    CreatePaymentCardRequest createRequest = createPaymentCardRequest();

    when(paymentCardRepository.existsByNumber("1234432112344321")).thenReturn(false);
    when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
    when(paymentCardRepository.countByUser_UserId(1L)).thenReturn(5);

    assertThrows(CardLimitExceededException.class, () -> service.createCard(createRequest));
  }

  @Test
  void shouldThrowIfCardWithSameNumberExists() {
    CreatePaymentCardRequest createRequest = createPaymentCardRequest();

    when(paymentCardRepository.existsByNumber("1234432112344321")).thenReturn(true);

    assertThrows(PaymentCardAlreadyExistsException.class, () -> service.createCard(createRequest));
  }

  @Test
  void shouldThrownIfCardNotFound() {
    when(paymentCardRepository.findById(2L)).thenReturn(Optional.empty());

    assertThrows(PaymentCardNotFoundException.class, () -> service.getCardById(2L));
  }

  private static CreatePaymentCardRequest createPaymentCardRequest() {
    CreatePaymentCardRequest createRequest = new CreatePaymentCardRequest();
    createRequest.setNumber("1234432112344321");
    createRequest.setHolder("Andrey");
    createRequest.setExpirationDate(LocalDate.of(2030, 2, 1));
    createRequest.setUserId(1L);
    return createRequest;
  }
}
