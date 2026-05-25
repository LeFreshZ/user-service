package com.innowise.userservice.dao;

import com.innowise.userservice.entity.PaymentCard;
import com.innowise.userservice.repository.PaymentCardRepository;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class PaymentCardDao {

  private final PaymentCardRepository repository;

  public PaymentCard save(PaymentCard card) {
    return repository.save(card);
  }

  public Optional<PaymentCard> findById(Long id) {
    return repository.findById(id);
  }

  public void delete(PaymentCard card) {
    repository.delete(card);
  }

  public boolean existsByNumber(String number) {
    return repository.existsByNumber(number);
  }

  public int countByUserId(Long userId) {
    return repository.countByUser_UserId(userId);
  }

  public Page<PaymentCard> findByUserId(Long userId, Pageable pageable) {
    return repository.findByUser_UserId(userId, pageable);
  }

  public Page<PaymentCard> findAllActive(Pageable pageable) {
    return repository.findAllActiveCards(pageable);
  }

  public Page<PaymentCard> searchByHolder(String holder, Pageable pageable) {
    return repository.searchByHolderNative(holder, pageable);
  }

  public Page<PaymentCard> findAll(Specification<PaymentCard> specification, Pageable pageable) {
    return repository.findAll(specification, pageable);
  }
}
