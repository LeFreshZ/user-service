package io.github.lefreshz.user_service.repository;

import io.github.lefreshz.user_service.entity.PaymentCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PaymentCardRepository extends JpaRepository<PaymentCard, Long>,
    JpaSpecificationExecutor<PaymentCard> {

}
