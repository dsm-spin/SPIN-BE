package com.example.spin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;

@Configuration
public class AnthropicConfig {

    @Bean
    public AnthropicClient anthropicClient(AnthropicProperties anthropicProperties) {
        return AnthropicOkHttpClient.builder().apiKey(anthropicProperties.apiKey()).build();
    }
}
