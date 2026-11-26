package com.innowise.userservice.mapper;

import com.innowise.userservice.dto.CreatePaymentCardRequest;
import com.innowise.userservice.dto.PaymentCardResponse;
import com.innowise.userservice.dto.UpdatePaymentCardRequest;
import com.innowise.userservice.entity.PaymentCard;
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
