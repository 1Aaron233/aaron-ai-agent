package com.aaron.aaronaiagent.app;

import com.aaron.aaronaiagent.advisor.MyLoggerAdvisor;
import com.aaron.aaronaiagent.rag.QueryRewriter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

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
            "引导用户详述具体情况、出生年月日时，以便给出专属命理解析与建议。" +
            "回答时保持专业、温和，明确区分传统命理参考与现实决策建议，避免绝对化结论。";

    public FortuneApp(ChatModel dashscopeChatModel) {
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(20)
                .build();
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
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
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    /**
     * AI 基础对话（支持多轮对话记忆，SSE 流式传输）
     */
    public Flux<String> doChatByStream(String message, String chatId) {
        return chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .stream()
                .content();
    }

    public record FortuneReport(String title, List<String> suggestions) {
    }

    /**
     * AI 命理报告功能（结构化输出）
     */
    public FortuneReport doChatWithReport(String message, String chatId) {
        FortuneReport fortuneReport = chatClient
                .prompt()
                .system(SYSTEM_PROMPT + "每次对话后都要生成命理结果，标题为{用户名}的命理报告，内容为建议列表")
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
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
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
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
