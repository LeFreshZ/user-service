package com.innowise.userservice.service;

import com.innowise.userservice.dto.CreatePaymentCardRequest;
import com.innowise.userservice.dto.PaymentCardResponse;
import com.innowise.userservice.dto.UpdatePaymentCardRequest;
import com.innowise.userservice.entity.PaymentCard;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service interface for managing payment cards.
 *
 * <p>Provides operations for creating, retrieving, updating, and deleting
 * payment cards, as well as managing their active status.
 */
public interface PaymentCardService {

  /**
   * Maximum number of payment cards allowed per user.
   */
  int CARD_LIMIT = 5;

  /**
   * Creates a new payment card for the specified user.
   *
   * <p>Evicts the cached entry for the associated user from the {@code "users"} cache
   * to ensure consistency after the card is added.
   *
   * @param request the request object containing card details and the target user ID
   * @return the created payment card as a {@link PaymentCardResponse}
   * @throws com.innowise.userservice.exception.PaymentCardAlreadyExistsException if a card with the
   *                                                                              given number
   *                                                                              already exists
   * @throws com.innowise.userservice.exception.UserNotFoundException             if no user with
   *                                                                              the given ID
   *                                                                              exists
   * @throws com.innowise.userservice.exception.CardLimitExceededException        if the user has
   *                                                                              already reached
   *                                                                              the card limit of
   *                                                                              {@value
   *                                                                              #CARD_LIMIT}
   */
  @Transactional
  @CacheEvict(value = "users", key = "#request.userId")
  PaymentCardResponse createCard(CreatePaymentCardRequest request);

  /**
   * Retrieves a paginated list of all active payment cards.
   *
   * @param pageable pagination and sorting parameters
   * @return a {@link Page} of active {@link PaymentCardResponse} objects
   */
  Page<PaymentCardResponse> getActiveCards(Pageable pageable);

  /**
   * Retrieves a paginated list of payment cards filtered by cardholder name.
   *
   * @param holder   the full or partial name of the cardholder
   * @param pageable pagination and sorting parameters
   * @return a {@link Page} of {@link PaymentCardResponse} objects matching the holder name
   */
  Page<PaymentCardResponse> getAllCardsByHolder(String holder, Pageable pageable);

  /**
   * Retrieves a payment card by its unique identifier.
   *
   * @param id the unique identifier of the payment card
   * @return the found payment card as a {@link PaymentCardResponse}
   * @throws com.innowise.userservice.exception.PaymentCardNotFoundException if no card with the
   *                                                                         given ID exists
   */
  PaymentCardResponse getCardById(long id);

  /**
   * Retrieves a paginated list of payment cards matching the given specification.
   *
   * @param specification the JPA specification used to filter cards
   * @param pageable      pagination and sorting parameters
   * @return a {@link Page} of {@link PaymentCardResponse} objects matching the specification
   */
  Page<PaymentCardResponse> getAllCards(Specification<PaymentCard> specification,
      Pageable pageable);

  /**
   * Retrieves a paginated list of payment cards belonging to a specific user.
   *
   * @param userId   the unique identifier of the user
   * @param pageable pagination and sorting parameters
   * @return a {@link Page} of {@link PaymentCardResponse} objects owned by the user
   * @throws com.innowise.userservice.exception.UserNotFoundException if no user with the given ID
   *                                                                  exists
   */
  Page<PaymentCardResponse> getCardsByUserId(long userId, Pageable pageable);

  /**
   * Updates an existing payment card with the provided data.
   *
   * <p>Evicts the cached entry for the associated user from the {@code "users"} cache
   * after the operation completes successfully.
   *
   * @param id      the unique identifier of the card to update
   * @param request the request object containing updated card fields
   * @return the updated payment card as a {@link PaymentCardResponse}
   * @throws com.innowise.userservice.exception.PaymentCardNotFoundException if no card with the
   *                                                                         given ID exists
   */
  @Transactional
  PaymentCardResponse updateCard(long id, UpdatePaymentCardRequest request);

  /**
   * Deletes a payment card by its unique identifier.
   *
   * <p>Evicts the cached entry for the associated user from the {@code "users"} cache
   * after the operation completes successfully.
   *
   * @param id the unique identifier of the card to delete
   * @throws com.innowise.userservice.exception.PaymentCardNotFoundException if no card with the
   *                                                                         given ID exists
   */
  @Transactional
  void deleteCard(long id);

  /**
   * Changes the active status of a payment card.
   *
   * <p>Evicts the cached entry for the associated user from the {@code "users"} cache
   * after the operation completes successfully.
   *
   * @param id     the unique identifier of the card
   * @param active {@code true} to activate the card, {@code false} to deactivate it
   * @return the updated payment card as a {@link PaymentCardResponse}
   * @throws com.innowise.userservice.exception.PaymentCardNotFoundException if no card with the
   *                                                                         given ID exists
   */
  @Transactional
  PaymentCardResponse changeActiveStatus(long id, boolean active);
}
