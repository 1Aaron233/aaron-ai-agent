package com.aaron.aaronaiagent.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientMessageAggregator;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import reactor.core.publisher.Flux;

/**
 * 自定义日志 Advisor
 * 打印 info 级别日志、只输出单次用户提示词和 AI 回复的文本
 */
@Slf4j
public class MyLoggerAdvisor implements CallAdvisor, StreamAdvisor {

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        return 0;
    }

    private void logRequest(ChatClientRequest request) {
        String userText = request.prompt() != null && request.prompt().getUserMessage() != null
                ? request.prompt().getUserMessage().getText()
                : "";
        log.info("AI Request: {}", userText);
    }

    private void logResponse(ChatClientResponse response) {
        if (response.chatResponse() != null
                && response.chatResponse().getResult() != null
                && response.chatResponse().getResult().getOutput() != null) {
            log.info("AI Response: {}", response.chatResponse().getResult().getOutput().getText());
        }
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain chain) {
        logRequest(chatClientRequest);
        ChatClientResponse chatClientResponse = chain.nextCall(chatClientRequest);
        logResponse(chatClientResponse);
        return chatClientResponse;
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest, StreamAdvisorChain chain) {
        logRequest(chatClientRequest);
        Flux<ChatClientResponse> chatClientResponses = chain.nextStream(chatClientRequest);
        return new ChatClientMessageAggregator().aggregateChatClientResponse(chatClientResponses, this::logResponse);
    }
}
