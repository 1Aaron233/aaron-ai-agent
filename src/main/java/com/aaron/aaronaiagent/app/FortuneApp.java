package com.aaron.aaronaiagent.app;

import com.aaron.aaronaiagent.advisor.MyLoggerAdvisor;
import com.aaron.aaronaiagent.rag.QueryRewriter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Component
@Slf4j
public class FortuneApp {

    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT = "扮演深耕命理玄学领域的算命大师。开场向用户表明身份，告知用户可倾诉命理困惑。" +
            "围绕命理、手相、风水、流年运势四个方向提问：" +
            "命理方向询问八字、五行、生肖运势的困惑；" +
            "手相方向询问掌纹、生命线、感情线的疑问；" +
            "风水方向询问居家、办公、财位布局的问题；" +
            "流年运势方向询问年度运势、太岁、桃花运的困扰。" +
            "引导用户详述具体情况、出生年月日时，以便给出专属命理解析与建议。";

    public FortuneApp(ChatModel dashscopeChatModel) {
        ChatMemory chatMemory = new InMemoryChatMemory();
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory),
                        new MyLoggerAdvisor()
                )
                .build();
    }

    /**
     * AI 基础对话（支持多轮对话记忆）
     */
    public String doChat(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    record FortuneReport(String title, List<String> suggestions) {
    }

    /**
     * AI 命理报告功能（结构化输出）
     */
    public FortuneReport doChatWithReport(String message, String chatId) {
        FortuneReport fortuneReport = chatClient
                .prompt()
                .system(SYSTEM_PROMPT + "每次对话后都要生成命理结果，标题为{用户名}的命理报告，内容为建议列表")
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .entity(FortuneReport.class);
        log.info("fortuneReport: {}", fortuneReport);
        return fortuneReport;
    }

    @Resource
    private VectorStore fortuneAppVectorStore;

    @Resource
    private Advisor fortuneAppRagCloudAdvisor;

    @Resource
    private QueryRewriter queryRewriter;

    /**
     * 和 RAG 知识库进行对话
     */
    public String doChatWithRag(String message, String chatId) {
        String rewrittenMessage = queryRewriter.doQueryRewrite(message);
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(rewrittenMessage)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .advisors(new MyLoggerAdvisor())
                .advisors(new QuestionAnswerAdvisor(fortuneAppVectorStore))
//                .advisors(fortuneAppRagCloudAdvisor)
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }
}
