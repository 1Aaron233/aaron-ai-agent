package com.aaron.aaronaiagent.demo.invoke;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * LangChain4j 调用 AI 测试
 * 注意：需要在 TestApiKey 中配置有效的 API Key
 */
public class LangChainAiInvokeTest {

    @Test
    void testChat() {
        ChatLanguageModel model = QwenChatModel.builder()
                .apiKey(TestApiKey.API_KEY)
                .modelName("qwen-max")
                .build();
        String answer = model.chat("你好，我是 Aaron");
        System.out.println("LangChain4j 调用结果：" + answer);
        Assertions.assertNotNull(answer);
    }
}
