package com.aaron.aaronaiagent.app;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

@SpringBootTest
class FortuneAppTest {

    @Resource
    private FortuneApp fortuneApp;

    @Test
    void testChat() {
        String chatId = UUID.randomUUID().toString();
        // 第一轮
        String message = "你好，我是程序员 Aaron";
        String answer = fortuneApp.doChat(message, chatId);
        // 第二轮
        message = "我想知道我今年的财运如何，我是 1995 年 8 月生的";
        answer = fortuneApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
        // 第三轮
        message = "我的出生年份你还记得吗？帮我回忆一下";
        answer = fortuneApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithReport() {
        String chatId = UUID.randomUUID().toString();
        String message = "你好，我是程序员 Aaron，我想了解一下我的命理运势，我是 1995 年 8 月出生的";
        FortuneApp.FortuneReport fortuneReport = fortuneApp.doChatWithReport(message, chatId);
        Assertions.assertNotNull(fortuneReport);
        System.out.println("命理报告：" + fortuneReport);
    }

    @Test
    void doChatWithRag() {
        String chatId = UUID.randomUUID().toString();
        String message = "我今年犯太岁，感觉运势很差，该怎么化解？";
        String answer = fortuneApp.doChatWithRag(message, chatId);
        Assertions.assertNotNull(answer);
        System.out.println("RAG 回答：" + answer);
    }
}
