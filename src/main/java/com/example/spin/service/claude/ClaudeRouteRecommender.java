package com.example.spin.service.claude;

import java.util.List;

import org.springframework.stereotype.Component;

import com.anthropic.client.AnthropicClient;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.StructuredMessage;
import com.anthropic.models.messages.StructuredMessageCreateParams;
import com.anthropic.models.messages.StructuredTextBlock;
import com.example.spin.config.AnthropicProperties;
import com.example.spin.domain.Store;

import lombok.RequiredArgsConstructor;

// 지역+목적을 프롬프트로 구성해 Claude에 전달하고, 후보 가게 중에서 순서가 있는 루트를 구조화된 출력으로 받아온다.
@Component
@RequiredArgsConstructor
public class ClaudeRouteRecommender {

    private static final String SYSTEM_PROMPT = """
            당신은 대전광역시 동네 나들이 루트를 추천하는 도우미입니다.
            사용자가 알려주는 지역과 목적에 맞춰, 함께 제공되는 후보 가게 목록 중에서만 \
            방문 순서가 있는 루트를 추천하세요 (보통 3곳이지만 상황에 따라 다를 수 있습니다).
            반드시 후보 목록에 있는 id만 사용하고, 목록에 없는 가게를 지어내지 마세요.
            목적은 반드시 지켜야 할 업종 필터가 아니라 루트 전체의 분위기와 테마를 정하는 힌트입니다. \
            같은 업종의 가게만 반복해서 나열하지 말고, 후보 중에 서로 다른 업종이 있다면 적극적으로 \
            섞어서 다채로운 루트를 구성하세요 (예: "혼자 커피 한 잔"이 목적이어도 카페는 한 곳만 넣고, \
            나머지는 소품샵, 베이커리, 서점처럼 그 분위기와 어울리는 다른 업종으로 채우는 식). \
            후보 목록에 업종 다양성이 전혀 없을 때만 같은 업종으로 채워도 됩니다.
            특히 식사(밥집, 고깃집, 국밥집 등 한 끼 식사가 되는 곳)는 카테고리 이름이 서로 달라도 \
            실제로는 같은 "식사" 경험이므로, 목적이 여러 맛집을 순서대로 둘러보는 것이 아닌 이상 \
            루트에는 식사 가게를 한 곳만 포함하세요. 나머지 자리는 카페, 디저트, 술집, 소품샵처럼 \
            식사와 겹치지 않는 다른 성격의 가게로 채우세요.
            후보 목록은 가까운 가게부터 먼 가게까지 고르게 섞여 있습니다. 주소가 완전히 같은 건물이거나 \
            바로 옆 상가인 가게들로만 루트를 채우면 나들이라기보다 제자리걸음처럼 느껴지니, \
            같은 골목/건물에 있는 가게들로만 묶지 말고 후보 목록 여기저기에 흩어진 가게를 골라 \
            적당히 걸어 다니는 느낌이 나는 루트를 구성하세요.
            각 가게의 업종은 가게 이름을 보고 판단해 "로스터리 카페", "이자카야", "수제버거" 같이 짧은 \
            카테고리로 표현하세요.
            각 가게의 추천 이유는 사용자의 목적과 자연스럽게 연결되는 한 문장으로 작성하세요.""";

    private final AnthropicClient anthropicClient;
    private final AnthropicProperties anthropicProperties;

    public RouteRecommendation recommend(String region, String purpose, List<Store> candidates) {
        String userMessage = buildUserMessage(region, purpose, candidates);

        StructuredMessageCreateParams<RouteRecommendation> params = MessageCreateParams.builder()
                .model(anthropicProperties.model())
                .maxTokens(16000L)
                .system(SYSTEM_PROMPT)
                .outputConfig(RouteRecommendation.class)
                .addUserMessage(userMessage)
                .build();

        StructuredMessage<RouteRecommendation> response = anthropicClient.messages().create(params);

        return response.content().stream()
                .flatMap(block -> block.text().stream())
                .findFirst()
                .map(StructuredTextBlock::text)
                .orElseThrow(() -> new IllegalStateException("Claude가 구조화된 응답을 반환하지 않았습니다"));
    }

    private String buildUserMessage(String region, String purpose, List<Store> candidates) {
        StringBuilder sb = new StringBuilder();
        sb.append("지역: ").append(region).append("\n");
        sb.append("목적: ").append(purpose).append("\n\n");
        sb.append("후보 목록:\n");
        for (Store store : candidates) {
            sb.append("- id: ").append(store.getId())
                    .append(", 이름: ").append(store.getName())
                    .append(", 주소: ").append(store.getAddress())
                    .append("\n");
        }
        return sb.toString();
    }
}
