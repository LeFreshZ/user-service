package io.github.lefreshz.user_service.mapper;

import io.github.lefreshz.user_service.dto.CreatePaymentCardRequest;
import io.github.lefreshz.user_service.dto.PaymentCardResponse;
import io.github.lefreshz.user_service.dto.UpdatePaymentCardRequest;
import io.github.lefreshz.user_service.entity.PaymentCard;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PaymentCardMapper {

  @Mapping(target = "user", ignore = true)
  PaymentCard toEntity(CreatePaymentCardRequest request);

  @Mapping(source = "user.userId", target = "userId")
  PaymentCardResponse toResponse(PaymentCard paymentCard);

  void updateCard(UpdatePaymentCardRequest request, @MappingTarget PaymentCard paymentCard);
}
