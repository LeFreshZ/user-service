package io.github.lefreshz.user_service.service;

import io.github.lefreshz.user_service.dto.CreatePaymentCardRequest;
import io.github.lefreshz.user_service.dto.PaymentCardResponse;
import io.github.lefreshz.user_service.dto.UpdatePaymentCardRequest;
import io.github.lefreshz.user_service.entity.PaymentCard;
import io.github.lefreshz.user_service.entity.User;
import io.github.lefreshz.user_service.exception.CardLimitExceededException;
import io.github.lefreshz.user_service.exception.PaymentCardNotFoundException;
import io.github.lefreshz.user_service.exception.UserNotFoundException;
import io.github.lefreshz.user_service.mapper.PaymentCardMapper;
import io.github.lefreshz.user_service.repository.PaymentCardRepository;
import io.github.lefreshz.user_service.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class PaymentCardService {

  private final PaymentCardMapper mapper;
  private final PaymentCardRepository paymentCardRepository;
  private final UserRepository userRepository;

  public PaymentCardResponse createCard(CreatePaymentCardRequest request) {
    Optional<User> optionalUser = userRepository.findById(request.getUserId());

    if (optionalUser.isEmpty()) {
      throw new UserNotFoundException("Can not find user with userId = " + request.getUserId());
    }

    User user = optionalUser.get();

    if (paymentCardRepository.countByUser_UserId(request.getUserId()) >= 5) {
      throw new CardLimitExceededException("User can not have more than 5 cards");
    }

    PaymentCard card = mapper.toEntity(request);
    card.setUser(user);
    card.setActive(true);

    PaymentCard savedCard = paymentCardRepository.save(card);

    return mapper.toResponse(savedCard);
  }

  public PaymentCardResponse getCardById(long id) {
    return mapper.toResponse(getPaymentCard(id));
  }

  public Page<PaymentCardResponse> getAllCards(Specification<PaymentCard> specification,
      Pageable pageable) {
    Page<PaymentCard> cardPage = paymentCardRepository.findAll(specification, pageable);

    return cardPage.map(mapper::toResponse);
  }

  public List<PaymentCardResponse> getCardsByUserId(long userId) {
    if (!userRepository.existsById(userId)) {
      throw new UserNotFoundException("Can not find user with userId = " + userId);
    }

    return paymentCardRepository.findByUser_UserId(userId).stream().map(mapper::toResponse)
        .toList();
  }

  public Page<PaymentCardResponse> getCardsByUserId(long userId, Pageable pageable) {
    if (!userRepository.existsById(userId)) {
      throw new UserNotFoundException("Can not find user with userId = " + userId);
    }

    return paymentCardRepository.findByUser_UserId(userId, pageable).map(mapper::toResponse);
  }

  @Transactional
  public PaymentCardResponse updateCard(long id, UpdatePaymentCardRequest request) {
    PaymentCard card = getPaymentCard(id);

    mapper.updateCard(request, card);

    PaymentCard savedCard = paymentCardRepository.save(card);

    return mapper.toResponse(savedCard);
  }

  @Transactional
  public PaymentCardResponse changeCardStatus(long id) {
    PaymentCard card = getPaymentCard(id);

    card.setActive(!card.getActive());

    PaymentCard savedCard = paymentCardRepository.save(card);

    return mapper.toResponse(savedCard);
  }

  @Transactional
  public void deleteCard(long id) {
    PaymentCard card = getPaymentCard(id);

    paymentCardRepository.delete(card);
  }

  private PaymentCard getPaymentCard(long id) {
    Optional<PaymentCard> optionalCard = paymentCardRepository.findById(id);

    if (optionalCard.isEmpty()) {
      throw new PaymentCardNotFoundException("Can not find payment card with cardId = " + id);
    }

    return optionalCard.get();
  }
}
