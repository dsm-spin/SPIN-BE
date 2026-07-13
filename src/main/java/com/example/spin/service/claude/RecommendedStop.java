package com.example.spin.service.claude;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record RecommendedStop(
        @JsonPropertyDescription("후보 목록에 있는 가게의 id") int storeId,
        @JsonPropertyDescription("가게 업종을 짧게 표현한 카테고리 (예: \"로스터리 카페\", \"이자카야\", \"수제버거\")") String category,
        @JsonPropertyDescription("이 가게를 추천하는 한 줄 이유") String reason) {
}
