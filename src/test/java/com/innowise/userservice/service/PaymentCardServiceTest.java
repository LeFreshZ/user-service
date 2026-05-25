package com.innowise.userservice.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.innowise.userservice.dao.PaymentCardDao;
import com.innowise.userservice.dao.UserDao;
import com.innowise.userservice.dto.CreatePaymentCardRequest;
import com.innowise.userservice.dto.PaymentCardResponse;
import com.innowise.userservice.dto.UpdatePaymentCardRequest;
import com.innowise.userservice.entity.PaymentCard;
import com.innowise.userservice.entity.User;
import com.innowise.userservice.exception.CardLimitExceededException;
import com.innowise.userservice.exception.PaymentCardAlreadyExistsException;
import com.innowise.userservice.exception.PaymentCardNotFoundException;
import com.innowise.userservice.exception.UserNotFoundException;
import com.innowise.userservice.mapper.PaymentCardMapper;
import com.innowise.userservice.service.implementation.PaymentCardServiceImpl;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
public class PaymentCardServiceTest {

  @Mock
  private PaymentCardDao paymentCardDao;

  @Mock
  private CacheManager cacheManager;

  @Mock
  private UserDao userDao;

  private PaymentCardService service;

  private PaymentCard card;

  @BeforeEach
  void setup() {
    PaymentCardMapper mapper = Mappers.getMapper(PaymentCardMapper.class);

    service = new PaymentCardServiceImpl(mapper, paymentCardDao, userDao, cacheManager);

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
    when(paymentCardDao.findById(1L)).thenReturn(Optional.of(card));

    PaymentCardResponse response = service.getCardById(1L);

    assertEquals("1234432112344321", response.getNumber());
    assertEquals("Andrey", response.getHolder());
  }

  @Test
  void shouldAddCard() {
    CreatePaymentCardRequest createRequest = createPaymentCardRequest();

    when(paymentCardDao.existsByNumber("1234432112344321")).thenReturn(false);
    when(userDao.findById(1L)).thenReturn(Optional.of(new User()));
    when(paymentCardDao.countByUserId(1L)).thenReturn(3);
    when(paymentCardDao.save(any(PaymentCard.class))).thenReturn(card);

    PaymentCardResponse response = service.createCard(createRequest);

    assertEquals("1234432112344321", response.getNumber());
    assertEquals("Andrey", response.getHolder());
    assertEquals(LocalDate.of(2030, 2, 1), response.getExpirationDate());
    verify(paymentCardDao).save(any(PaymentCard.class));
  }

  @Test
  void shouldCorrectlyUpdateCard() {
    UpdatePaymentCardRequest updateRequest = new UpdatePaymentCardRequest();
    updateRequest.setHolder("Kate");
    updateRequest.setExpirationDate(LocalDate.of(2035, 8, 2));

    when(paymentCardDao.findById(1L)).thenReturn(Optional.of(card));
    when(paymentCardDao.save(any(PaymentCard.class))).thenAnswer(
        invocation -> invocation.getArgument(0));

    PaymentCardResponse response = service.updateCard(1L, updateRequest);

    assertEquals("Kate", response.getHolder());
    assertEquals(LocalDate.of(2035, 8, 2), response.getExpirationDate());
  }

  @Test
  void shouldCorrectlyDeleteCard() {
    when(paymentCardDao.findById(1L)).thenReturn(Optional.of(card));

    service.deleteCard(1L);

    verify(paymentCardDao).delete(card);
  }

  @Test
  void shouldChangeActiveStatus() {
    when(paymentCardDao.findById(1L)).thenReturn(Optional.of(card));
    when(paymentCardDao.save(any(PaymentCard.class))).thenAnswer(
        invocation -> invocation.getArgument(0));

    PaymentCardResponse response = service.changeActiveStatus(1L, false);

    assertEquals(false, response.getActive());
  }

  @Test
  void shouldThrowIfNoUserFound() {
    CreatePaymentCardRequest createRequest = createPaymentCardRequest();

    when(paymentCardDao.existsByNumber("1234432112344321")).thenReturn(false);
    when(userDao.findById(1L)).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> service.createCard(createRequest));
  }

  @Test
  void shouldThrowIfUserHasFiveCards() {
    CreatePaymentCardRequest createRequest = createPaymentCardRequest();

    when(paymentCardDao.existsByNumber("1234432112344321")).thenReturn(false);
    when(userDao.findById(1L)).thenReturn(Optional.of(new User()));
    when(paymentCardDao.countByUserId(1L)).thenReturn(5);

    assertThrows(CardLimitExceededException.class, () -> service.createCard(createRequest));
  }

  @Test
  void shouldThrowIfCardWithSameNumberExists() {
    CreatePaymentCardRequest createRequest = createPaymentCardRequest();

    when(paymentCardDao.existsByNumber("1234432112344321")).thenReturn(true);

    assertThrows(PaymentCardAlreadyExistsException.class, () -> service.createCard(createRequest));
  }

  @Test
  void shouldThrownIfCardNotFound() {
    when(paymentCardDao.findById(2L)).thenReturn(Optional.empty());

    assertThrows(PaymentCardNotFoundException.class, () -> service.getCardById(2L));
  }

  @Test
  void shouldReturnActiveCards() {
    PageRequest pageable = PageRequest.of(0, 10);
    Page<PaymentCard> paymentCards = new PageImpl<>(List.of(card));

    when(paymentCardDao.findAllActive(pageable)).thenReturn(paymentCards);

    Page<PaymentCardResponse> response = service.getActiveCards(pageable);

    assertEquals(1, response.getTotalElements());
    assertEquals("1234432112344321", response.getContent().get(0).getNumber());
  }

  @Test
  void shouldReturnCardsByHolder() {
    PageRequest pageable = PageRequest.of(0, 10);
    Page<PaymentCard> paymentCards = new PageImpl<>(List.of(card));

    when(paymentCardDao.searchByHolder("Andrey", pageable)).thenReturn(paymentCards);

    Page<PaymentCardResponse> response = service.getAllCardsByHolder("Andrey", pageable);

    assertEquals(1, response.getTotalElements());
    assertEquals("Andrey", response.getContent().get(0).getHolder());
  }

  @Test
  void shouldReturnAllCardsBySpecification() {
    PageRequest pageable = PageRequest.of(0, 10);
    Specification<PaymentCard> specification = (root, query, cb) -> null;
    Page<PaymentCard> paymentCards = new PageImpl<>(List.of(card));

    when(paymentCardDao.findAll(specification, pageable)).thenReturn(paymentCards);

    Page<PaymentCardResponse> response = service.getAllCards(specification, pageable);

    assertEquals(1, response.getTotalElements());
    assertEquals("1234432112344321", response.getContent().get(0).getNumber());
  }

  @Test
  void shouldReturnCardsByUserId() {
    PageRequest pageable = PageRequest.of(0, 10);
    Page<PaymentCard> paymentCards = new PageImpl<>(List.of(card));

    when(userDao.existsById(1L)).thenReturn(true);
    when(paymentCardDao.findByUserId(1L, pageable)).thenReturn(paymentCards);

    Page<PaymentCardResponse> response = service.getCardsByUserId(1L, pageable);

    assertEquals(1, response.getTotalElements());
    assertEquals("1234432112344321", response.getContent().get(0).getNumber());
  }

  @Test
  void shouldThrowWhenUserIdNotFound() {
    PageRequest pageable = PageRequest.of(0, 10);

    when(userDao.existsById(2L)).thenReturn(false);

    assertThrows(UserNotFoundException.class, () -> service.getCardsByUserId(2L, pageable));
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
