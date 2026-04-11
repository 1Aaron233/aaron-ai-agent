package com.aaron.aaronaiagent.demo.invoke;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class SpringAiAiInvokeTest {

    @Resource
    private ChatModel dashscopeChatModel;

    @Test
    void testChat() {
        AssistantMessage assistantMessage = dashscopeChatModel.call(new Prompt("你好，我是Aaron"))
                .getResult()
                .getOutput();
        System.out.println(assistantMessage.getText());
    }
}
