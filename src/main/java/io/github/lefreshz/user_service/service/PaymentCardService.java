package io.github.lefreshz.user_service.service;

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
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
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
  private final CacheManager cacheManager;

  @CacheEvict(value = "users", key = "#request.userId")
  public PaymentCardResponse createCard(CreatePaymentCardRequest request) {
    if (paymentCardRepository.existsByNumber(request.getNumber())) {
      throw new PaymentCardAlreadyExistsException(
          "Payment card already exists with number: " + request.getNumber());
    }

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

  public Page<PaymentCardResponse> getActiveCards(Pageable pageable) {
    return paymentCardRepository.findAllActiveCards(pageable).map(mapper::toResponse);
  }

  public Page<PaymentCardResponse> getAllCardsByHolder(String holder, Pageable pageable) {
    return paymentCardRepository.searchByHolderNative(holder, pageable).map(mapper::toResponse);
  }

  public PaymentCardResponse getCardById(long id) {
    return mapper.toResponse(getPaymentCard(id));
  }

  public Page<PaymentCardResponse> getAllCards(Specification<PaymentCard> specification,
      Pageable pageable) {
    Page<PaymentCard> cardPage = paymentCardRepository.findAll(specification, pageable);

    return cardPage.map(mapper::toResponse);
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

    evictCache(savedCard.getUser().getUserId());

    return mapper.toResponse(savedCard);
  }

  @Transactional
  public void deleteCard(long id) {
    PaymentCard card = getPaymentCard(id);

    paymentCardRepository.delete(card);

    evictCache(card.getUser().getUserId());
  }

  @Transactional
  public PaymentCardResponse changeActiveStatus(long id) {
    PaymentCard card = getPaymentCard(id);

    card.setActive(!card.getActive());

    PaymentCard savedCard = paymentCardRepository.save(card);

    evictCache(savedCard.getUser().getUserId());

    return mapper.toResponse(savedCard);
  }

  private PaymentCard getPaymentCard(long id) {
    Optional<PaymentCard> optionalCard = paymentCardRepository.findById(id);

    if (optionalCard.isEmpty()) {
      throw new PaymentCardNotFoundException("Can not find payment card with cardId = " + id);
    }

    return optionalCard.get();
  }

  private void evictCache(long id) {
    Cache userCache = cacheManager.getCache("users");

    if (userCache != null) {
      userCache.evict(id);
    }
  }
}
