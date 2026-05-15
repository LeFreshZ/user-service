package io.github.lefreshz.user_service.specification;

import io.github.lefreshz.user_service.entity.PaymentCard;
import org.springframework.data.jpa.domain.Specification;

public class PaymentCardSpecification {

  public static Specification<PaymentCard> hasHolder(String holder) {
    return ((root, query, criteriaBuilder) ->
        holder == null ? null : criteriaBuilder.like(criteriaBuilder.lower(root.get("holder")),
            "%" + holder.toLowerCase() + "%")
    );
  }

  public static Specification<PaymentCard> isActive(Boolean active) {
    return ((root, query, criteriaBuilder) ->
        active == null ? null : criteriaBuilder.equal(root.get("is_active"), active)
        );
  }

  public static Specification<PaymentCard> hasUserId(Long userId) {
    return ((root, query, criteriaBuilder) ->
        userId == null ? null : criteriaBuilder.equal(root.get("user").get("user_id"), userId)
        );
  }
}
