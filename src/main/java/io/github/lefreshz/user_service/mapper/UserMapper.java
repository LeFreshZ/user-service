package io.github.lefreshz.user_service.mapper;

import io.github.lefreshz.user_service.dto.CreateUserRequest;
import io.github.lefreshz.user_service.dto.UpdateUserRequest;
import io.github.lefreshz.user_service.dto.UserResponse;
import io.github.lefreshz.user_service.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {

  User toEntity(CreateUserRequest request);

  UserResponse toResponse(User user);

  void updateUser(UpdateUserRequest request, @MappingTarget User user);
}
