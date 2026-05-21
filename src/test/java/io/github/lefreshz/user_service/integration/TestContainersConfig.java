package io.github.lefreshz.user_service.integration;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

public class TestContainersConfig {

  public static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(
      "postgres:16-alpine")
      .withDatabaseName("user_db")
      .withUsername("user")
      .withPassword("password");


  public static final GenericContainer<?> REDIS = new GenericContainer<>("redis:7.4-alpine")
      .withExposedPorts(6379);

  static {
    POSTGRES.start();
    REDIS.start();
  }
}
