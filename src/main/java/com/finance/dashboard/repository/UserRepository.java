package com.finance.dashboard.repository;

import com.finance.dashboard.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    @Query(value = "SELECT * FROM users WHERE username = :username", nativeQuery = true)
    Optional<User> findByUsernameIncludingDeleted(@Param("username") String username);

    @Query(value = "SELECT * FROM users WHERE email = :email", nativeQuery = true)
    Optional<User> findByEmailIncludingDeleted(@Param("email") String email);

    @Query(value = "DELETE FROM users WHERE deleted = true AND deleted_at < :date", nativeQuery = true)
    void purgeOldUsers(@Param("date") LocalDateTime date);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
