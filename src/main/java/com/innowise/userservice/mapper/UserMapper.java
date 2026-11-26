package com.innowise.userservice.mapper;

import com.innowise.userservice.dto.CreateUserRequest;
import com.innowise.userservice.dto.UpdateUserRequest;
import com.innowise.userservice.dto.UserResponse;
import com.innowise.userservice.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(
    componentModel = "spring",
    uses = PaymentCardMapper.class
)
public interface UserMapper {

  User toEntity(CreateUserRequest request);

  UserResponse toResponse(User user);

  void updateUser(UpdateUserRequest request, @MappingTarget User user);
}
