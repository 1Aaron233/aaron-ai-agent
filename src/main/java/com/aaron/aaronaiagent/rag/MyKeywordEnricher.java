package com.aaron.aaronaiagent.rag;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.KeywordMetadataEnricher;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 基于 AI 的文档元信息增强器（为文档补充元信息）
 */
@Slf4j
@Component
public class MyKeywordEnricher {

    @Resource
    private ChatModel dashscopeChatModel;

    // 每次请求之间的间隔（毫秒）
    private static final long REQUEST_INTERVAL_MS = 1000;

    public List<Document> enrichDocuments(List<Document> documents) {
        KeywordMetadataEnricher keywordMetadataEnricher = new KeywordMetadataEnricher(dashscopeChatModel, 5);
        List<Document> enrichedDocuments = new ArrayList<>();
        for (int i = 0; i < documents.size(); i++) {
            enrichedDocuments.addAll(keywordMetadataEnricher.apply(List.of(documents.get(i))));
            if (i < documents.size() - 1) {
                try {
                    Thread.sleep(REQUEST_INTERVAL_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("关键词增强被中断，已处理 {}/{} 个文档", i + 1, documents.size());
                    break;
                }
            }
        }
        return enrichedDocuments;
    }
}
