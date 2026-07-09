package com.innowise.userservice.controller;

import com.innowise.userservice.dto.ChangeActiveStatusRequest;
import com.innowise.userservice.dto.CreatePaymentCardRequest;
import com.innowise.userservice.dto.PaymentCardResponse;
import com.innowise.userservice.dto.UpdatePaymentCardRequest;
import com.innowise.userservice.entity.PaymentCard;
import com.innowise.userservice.service.PaymentCardService;
import com.innowise.userservice.specification.PaymentCardSpecification;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
  @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and principal == #request.userId)")
  public ResponseEntity<PaymentCardResponse> createCard(@Valid @RequestBody
  CreatePaymentCardRequest request) {
    PaymentCardResponse response = service.createCard(request);

    return ResponseEntity.status(201).body(response);
  }

  @GetMapping("/active")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Page<PaymentCardResponse>> getActiveCards(Pageable pageable) {
    return ResponseEntity.ok(service.getActiveCards(pageable));
  }

  @GetMapping("/search/holder")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Page<PaymentCardResponse>> getCardsByHolder(
      @RequestParam String holder,
      Pageable pageable) {

    return ResponseEntity.ok(service.getAllCardsByHolder(holder, pageable));
  }

  @GetMapping("/{cardId}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<PaymentCardResponse> getCardById(@PathVariable long cardId) {
    PaymentCardResponse response = service.getCardById(cardId);

    return ResponseEntity.ok(response);
  }

  @GetMapping("/user/{userId}")
  @PreAuthorize("hasRole('ADMIN') or principal == #userId")
  public ResponseEntity<Page<PaymentCardResponse>> getCardsByUserId(
      @PathVariable long userId,
      Pageable pageable) {

    Page<PaymentCardResponse> responses = service.getCardsByUserId(userId, pageable);

    return ResponseEntity.ok(responses);
  }

  @PutMapping("/{cardId}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<PaymentCardResponse> updateCard(
      @Valid @RequestBody UpdatePaymentCardRequest request,
      @PathVariable long cardId) {

    PaymentCardResponse response = service.updateCard(cardId, request);

    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{cardId}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> deleteCard(@PathVariable long cardId) {
    service.deleteCard(cardId);

    return ResponseEntity.noContent().build();
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
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

  @PatchMapping("/{cardId}/active")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<PaymentCardResponse> changeActiveStatus(
      @PathVariable long cardId,
      @Valid @RequestBody ChangeActiveStatusRequest request) {

    PaymentCardResponse response = service.changeActiveStatus(cardId, request.getActive());

    return ResponseEntity.ok(response);
  }
}
