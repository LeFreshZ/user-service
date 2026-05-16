package io.github.lefreshz.user_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users", indexes = {@Index(name = "idx_users_email", columnList = "email")})
@Getter
@Setter
public class User extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "user_id")
  private Long userId;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String surname;

  @Column(name = "birth_date", nullable = false)
  private LocalDate birthDate;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(name = "is_active", nullable = false)
  private Boolean active;

  @OneToMany(mappedBy = "user")
  private List<PaymentCard> paymentCards;
}
