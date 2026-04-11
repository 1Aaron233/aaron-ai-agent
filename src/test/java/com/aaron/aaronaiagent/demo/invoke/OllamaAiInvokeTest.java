package com.aaron.aaronaiagent.demo.invoke;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Ollama 本地模型调用测试
 * 注意：需要本地启动 Ollama 服务，并拉取对应模型（默认 gemma3:1b）
 * 启动命令：ollama serve
 * 拉取模型：ollama pull gemma3:1b
 */
@SpringBootTest
public class OllamaAiInvokeTest {

    @Resource
    private ChatModel ollamaChatModel;

    @Test
    void testChat() {
        AssistantMessage assistantMessage = ollamaChatModel.call(new Prompt("你好，我是 Aaron"))
                .getResult()
                .getOutput();
        System.out.println("Ollama 调用结果：" + assistantMessage.getText());
        Assertions.assertNotNull(assistantMessage.getText());
    }
}
