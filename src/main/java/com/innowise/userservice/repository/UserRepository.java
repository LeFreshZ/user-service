package com.innowise.userservice.repository;

import com.innowise.userservice.entity.User;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

  Optional<User> findByEmail(String email);

  boolean existsByEmail(String email);

  Page<User> findByActiveTrue(Pageable pageable);

  @Query(name = "User.searchByName")
  Page<User> searchByName(@Param("name") String name, Pageable pageable);

  @Query(value = "SELECT * FROM users WHERE surname ILIKE CONCAT('%', :surname, '%')", nativeQuery = true)
  Page<User> searchBySurnameNative(@Param("surname") String surname, Pageable pageable);

  @Query("SELECT u FROM User u LEFT JOIN FETCH u.paymentCards WHERE u.userId = :id")
  Optional<User> findByIdWithCards(@Param("id") long id);
}
