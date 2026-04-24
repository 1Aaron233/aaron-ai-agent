package com.aaron.aaronaiagent.controller;

import com.aaron.aaronaiagent.app.FortuneApp;
import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private FortuneApp fortuneApp;

    /**
     * 同步调用 AI 算命大师应用
     */
    @GetMapping("/fortune_app/chat/sync")
    public String doChatWithFortuneAppSync(String message, String chatId) {
        return fortuneApp.doChat(message, chatId);
    }

    /**
     * SSE 流式调用 AI 算命大师应用
     */
    @GetMapping(value = "/fortune_app/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithFortuneAppSse(String message, String chatId) {
        return fortuneApp.doChatByStream(message, chatId)
                .concatWith(Flux.just("[DONE]"));
    }

    /**
     * 命理报告
     */
    @GetMapping("/fortune_app/report")
    public FortuneApp.FortuneReport doChatWithFortuneReport(String message, String chatId) {
        return fortuneApp.doChatWithReport(message, chatId);
    }

    /**
     * RAG 知识库问答
     */
    @GetMapping("/fortune_app/chat/rag")
    public String doChatWithFortuneAppRag(String message, String chatId) {
        return fortuneApp.doChatWithRag(message, chatId);
    }
}
