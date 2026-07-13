package com.example.spin.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.spin.domain.User;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByAccountId(String accountId);
}
