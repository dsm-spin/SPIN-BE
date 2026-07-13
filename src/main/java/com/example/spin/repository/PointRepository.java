package com.example.spin.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.spin.domain.Point;
import com.example.spin.domain.User;

public interface PointRepository extends JpaRepository<Point, Integer> {

    Optional<Point> findByUser(User user);
}
