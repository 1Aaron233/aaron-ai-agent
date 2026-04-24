package com.aaron.aaronaiagent.controller;

import com.aaron.aaronaiagent.app.FortuneApp;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AiController.class)
class AiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FortuneApp fortuneApp;

    @Test
    void shouldReturnSyncChatResponse() throws Exception {
        given(fortuneApp.doChat(anyString(), anyString())).willReturn("命理分析结果");

        mockMvc.perform(get("/ai/fortune_app/chat/sync")
                        .param("message", "我想看看事业运")
                        .param("chatId", "fortune_test"))
                .andExpect(status().isOk())
                .andExpect(content().string("命理分析结果"));
    }

    @Test
    void shouldReturnReport() throws Exception {
        given(fortuneApp.doChatWithReport(anyString(), anyString()))
                .willReturn(new FortuneApp.FortuneReport("Aaron 的命理报告", java.util.List.of("宜稳中求进")));

        mockMvc.perform(get("/ai/fortune_app/report")
                        .param("message", "帮我生成命理报告")
                        .param("chatId", "fortune_test"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {"title":"Aaron 的命理报告","suggestions":["宜稳中求进"]}
                        """));
    }
}
