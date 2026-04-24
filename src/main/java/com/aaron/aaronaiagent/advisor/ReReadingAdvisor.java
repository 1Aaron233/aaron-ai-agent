package com.aaron.aaronaiagent.advisor;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.prompt.PromptTemplate;
import reactor.core.publisher.Flux;

import java.util.Map;

/**
 * 自定义 Re2 Advisor
 * 可提高大型语言模型的推理能力
 */
public class ReReadingAdvisor implements CallAdvisor, StreamAdvisor {

    /**
     * 执行请求前，改写 Prompt
     */
    private ChatClientRequest before(ChatClientRequest chatClientRequest) {
        String userText = chatClientRequest.prompt().getUserMessage().getText();
        String augmentedUserText = PromptTemplate.builder()
                .template("""
                        {re2_input_query}
                        Read the question again: {re2_input_query}
                        """)
                .variables(Map.of("re2_input_query", userText))
                .build()
                .render();

        return chatClientRequest.mutate()
                .prompt(chatClientRequest.prompt().augmentUserMessage(augmentedUserText))
                .build();
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain chain) {
        return chain.nextCall(this.before(chatClientRequest));
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest, StreamAdvisorChain chain) {
        return chain.nextStream(this.before(chatClientRequest));
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }
}
