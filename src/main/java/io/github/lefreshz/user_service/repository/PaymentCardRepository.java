package io.github.lefreshz.user_service.repository;

import io.github.lefreshz.user_service.entity.PaymentCard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentCardRepository extends JpaRepository<PaymentCard, Long>,
    JpaSpecificationExecutor<PaymentCard> {

  Page<PaymentCard> findByUser_UserId(Long userId, Pageable pageable);

  int countByUser_UserId(Long userId);

  boolean existsByNumber(String number);

  @Query("SELECT c FROM PaymentCard c WHERE c.active = true")
  Page<PaymentCard> findAllActiveCards(Pageable pageable);

  @Query(value = "SELECT * FROM payment_cards WHERE holder ILIKE CONCAT('%', :holder, '%')", nativeQuery = true)
  Page<PaymentCard> searchByHolderNative(@Param("holder") String holder, Pageable pageable);
}
