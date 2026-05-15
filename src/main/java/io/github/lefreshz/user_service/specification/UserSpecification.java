package io.github.lefreshz.user_service.specification;

import io.github.lefreshz.user_service.entity.User;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {

  public static Specification<User> hasName(String name) {
    return ((root, query, criteriaBuilder) ->
        name == null ? null : criteriaBuilder.like(criteriaBuilder.lower(root.get("name")),
            "%" + name.toLowerCase() + "%")
    );
  }

  public static Specification<User> hasSurname(String surname) {
    return ((root, query, criteriaBuilder) ->
        surname == null ? null : criteriaBuilder.like(criteriaBuilder.lower(root.get("surname")),
            "%" + surname.toLowerCase() + "%")
    );
  }
}
