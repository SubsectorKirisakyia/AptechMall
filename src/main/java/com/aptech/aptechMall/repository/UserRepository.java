package com.aptech.aptechMall.repository;

import com.aptech.aptechMall.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for User entity
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by email
     * @param email User email
     * @return Optional containing User if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if user exists by email
     * @param email User email
     * @return true if exists
     */
    boolean existsByEmail(String email);
}
