package com.aaron.aaronaiagent.rag;

import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;

/**
 * 创建自定义的 RAG 检索增强顾问的工厂
 */
public class FortuneAppRagCustomAdvisorFactory {

    /**
     * 创建自定义的 RAG 检索增强顾问
     *
     * @param vectorStore 向量存储
     * @param status      分类标签（如：命理、手相、风水、运势）
     * @return 自定义的 RAG 检索增强顾问
     */
    public static Advisor createFortuneAppRagCustomAdvisor(VectorStore vectorStore, String status) {
        Filter.Expression expression = new FilterExpressionBuilder()
                .eq("status", status)
                .build();
        DocumentRetriever documentRetriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .filterExpression(expression)
                .similarityThreshold(0.5)
                .topK(3)
                .build();
        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(documentRetriever)
                .queryAugmenter(FortuneAppContextualQueryAugmenterFactory.createInstance())
                .build();
    }
}
