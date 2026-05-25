package com.innowise.userservice.service.implementation;

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
import com.innowise.userservice.service.PaymentCardService;
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
public class PaymentCardServiceImpl implements PaymentCardService {

  private final PaymentCardMapper mapper;
  private final PaymentCardDao paymentCardDao;
  private final UserDao userDao;
  private final CacheManager cacheManager;

  @Transactional
  @CacheEvict(value = "users", key = "#request.userId")
  @Override
  public PaymentCardResponse createCard(CreatePaymentCardRequest request) {
    if (paymentCardDao.existsByNumber(request.getNumber())) {
      throw new PaymentCardAlreadyExistsException(
          "Payment card already exists with number: " + request.getNumber());
    }

    Optional<User> optionalUser = userDao.findById(request.getUserId());

    if (optionalUser.isEmpty()) {
      throw new UserNotFoundException("Can not find user with userId = " + request.getUserId());
    }

    User user = optionalUser.get();

    if (paymentCardDao.countByUserId(request.getUserId()) >= CARD_LIMIT) {
      throw new CardLimitExceededException("User can not have more than 5 cards");
    }

    PaymentCard card = mapper.toEntity(request);
    card.setUser(user);
    card.setActive(true);

    PaymentCard savedCard = paymentCardDao.save(card);

    return mapper.toResponse(savedCard);
  }

  @Override
  public Page<PaymentCardResponse> getActiveCards(Pageable pageable) {
    return paymentCardDao.findAllActive(pageable).map(mapper::toResponse);
  }

  @Override
  public Page<PaymentCardResponse> getAllCardsByHolder(String holder, Pageable pageable) {
    return paymentCardDao.searchByHolder(holder, pageable).map(mapper::toResponse);
  }

  @Override
  public PaymentCardResponse getCardById(long id) {
    return mapper.toResponse(getPaymentCard(id));
  }

  @Override
  public Page<PaymentCardResponse> getAllCards(Specification<PaymentCard> specification,
      Pageable pageable) {
    Page<PaymentCard> cardPage = paymentCardDao.findAll(specification, pageable);

    return cardPage.map(mapper::toResponse);
  }

  @Override
  public Page<PaymentCardResponse> getCardsByUserId(long userId, Pageable pageable) {
    if (!userDao.existsById(userId)) {
      throw new UserNotFoundException("Can not find user with userId = " + userId);
    }

    return paymentCardDao.findByUserId(userId, pageable).map(mapper::toResponse);
  }

  @Transactional
  @Override
  public PaymentCardResponse updateCard(long id, UpdatePaymentCardRequest request) {
    PaymentCard card = getPaymentCard(id);

    mapper.updateCard(request, card);

    PaymentCard savedCard = paymentCardDao.save(card);

    evictCache(savedCard.getUser().getUserId());

    return mapper.toResponse(savedCard);
  }

  @Transactional
  @Override
  public void deleteCard(long id) {
    PaymentCard card = getPaymentCard(id);

    paymentCardDao.delete(card);

    evictCache(card.getUser().getUserId());
  }

  @Transactional
  @Override
  public PaymentCardResponse changeActiveStatus(long id, boolean active) {
    PaymentCard card = getPaymentCard(id);

    card.setActive(active);

    PaymentCard savedCard = paymentCardDao.save(card);

    evictCache(savedCard.getUser().getUserId());

    return mapper.toResponse(savedCard);
  }

  private PaymentCard getPaymentCard(long id) {
    Optional<PaymentCard> optionalCard = paymentCardDao.findById(id);

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
