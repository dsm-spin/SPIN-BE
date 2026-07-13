package com.example.spin.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 유저 한 명당 한 줄, 잔액만 관리한다. 적립 내역은 안 남기고 잔액만 누적한다.
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Point {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    private int balance;

    public Point(User user) {
        this(0, user, 0);
    }

    public void earn(int amount) {
        this.balance += amount;
    }
}
