package com.example.spin.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;

    private String region;

    private String address;

    private Double latitude;

    private Double longitude;

    private String benefit;

    @Column(unique = true)
    private String qrCode;

    public Store(String name, String region, String address, Double latitude, Double longitude,
            String benefit, String qrCode) {
        this(0, name, region, address, latitude, longitude, benefit, qrCode);
    }
}
