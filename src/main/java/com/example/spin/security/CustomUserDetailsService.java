package com.example.spin.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.spin.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String accountId) {
        com.example.spin.domain.User user = userRepository.findByAccountId(accountId)
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 계정입니다: " + accountId));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getAccountId())
                .password(user.getPassword())
                .authorities("ROLE_USER")
                .build();
    }
}
