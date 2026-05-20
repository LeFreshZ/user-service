package io.github.lefreshz.user_service.controller;

import io.github.lefreshz.user_service.dto.CreatePaymentCardRequest;
import io.github.lefreshz.user_service.dto.PaymentCardResponse;
import io.github.lefreshz.user_service.dto.UpdatePaymentCardRequest;
import io.github.lefreshz.user_service.entity.PaymentCard;
import io.github.lefreshz.user_service.exception.CardLimitExceededException;
import io.github.lefreshz.user_service.exception.PaymentCardAlreadyExistsException;
import io.github.lefreshz.user_service.exception.PaymentCardNotFoundException;
import io.github.lefreshz.user_service.exception.UserNotFoundException;
import io.github.lefreshz.user_service.service.PaymentCardService;
import io.github.lefreshz.user_service.specification.PaymentCardSpecification;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payment-cards")
@AllArgsConstructor
public class PaymentCardController {

  private final PaymentCardService service;

  @PostMapping
  public ResponseEntity<PaymentCardResponse> createCard(@Valid @RequestBody
  CreatePaymentCardRequest request) {
    PaymentCardResponse response;

    try {
      response = service.createCard(request);
    } catch (PaymentCardAlreadyExistsException | CardLimitExceededException ex) {
      return ResponseEntity.status(409).build();
    } catch (UserNotFoundException ex) {
      return ResponseEntity.notFound().build();
    }

    return ResponseEntity.status(201).body(response);
  }

  @GetMapping("/active")
  public ResponseEntity<Page<PaymentCardResponse>> getActiveCards(Pageable pageable) {
    return ResponseEntity.ok(service.getActiveCards(pageable));
  }

  @GetMapping("/search/holder")
  public ResponseEntity<Page<PaymentCardResponse>> getCardsByHolder(
      @RequestParam String holder,
      Pageable pageable) {

    return ResponseEntity.ok(service.getAllCardsByHolder(holder, pageable));
  }

  @GetMapping("/{cardId}")
  public ResponseEntity<PaymentCardResponse> getCardById(@PathVariable long cardId) {
    PaymentCardResponse response;

    try {
      response = service.getCardById(cardId);
    } catch (PaymentCardNotFoundException ex) {
      return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok(response);
  }

  @GetMapping("/user/{userId}")
  public ResponseEntity<Page<PaymentCardResponse>> getCardsByUserId(
      @PathVariable long userId,
      Pageable pageable) {

    Page<PaymentCardResponse> responses;

    try {
      responses = service.getCardsByUserId(userId, pageable);
    } catch (UserNotFoundException ex) {
      return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok(responses);
  }

  @PutMapping("/{cardId}")
  public ResponseEntity<PaymentCardResponse> updateCard(
      @Valid @RequestBody UpdatePaymentCardRequest request,
      @PathVariable long cardId) {

    PaymentCardResponse response;

    try {
      response = service.updateCard(cardId, request);
    } catch (PaymentCardNotFoundException ex) {
      return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{cardId}")
  public ResponseEntity<Void> deleteCard(@PathVariable long cardId) {
    try {
      service.deleteCard(cardId);
    } catch (PaymentCardNotFoundException ex) {
      return ResponseEntity.notFound().build();
    }

    return ResponseEntity.noContent().build();
  }

  @GetMapping
  public ResponseEntity<Page<PaymentCardResponse>> getAllCards(
      @RequestParam(required = false) String holder,
      @RequestParam(required = false) Long userId,
      @RequestParam(required = false) Boolean active,
      Pageable pageable
  ) {

    Specification<PaymentCard> specification =
        Specification.allOf(
            PaymentCardSpecification.hasHolder(holder),
            PaymentCardSpecification.hasUserId(userId),
            PaymentCardSpecification.isActive(active));

    return ResponseEntity.ok(service.getAllCards(specification, pageable));
  }

  @PatchMapping("/{id}/active")
  public ResponseEntity<PaymentCardResponse> changeActiveStatus(@PathVariable long id) {
    PaymentCardResponse response;

    try {
      response = service.changeActiveStatus(id);
    } catch (PaymentCardNotFoundException ex) {
      return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok(response);
  }
}
