package com.aaron.aaronaiagent.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 算命大师向量数据库配置（初始化基于内存的向量数据库 Bean）
 */
@Configuration
public class FortuneAppVectorStoreConfig {

    @Resource
    private FortuneAppDocumentLoader fortuneAppDocumentLoader;

    @Resource
    private MyTokenTextSplitter myTokenTextSplitter;

    @Resource
    private MyKeywordEnricher myKeywordEnricher;

    @Bean
    VectorStore fortuneAppVectorStore(EmbeddingModel dashscopeEmbeddingModel) {
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(dashscopeEmbeddingModel).build();
        List<Document> documentList = fortuneAppDocumentLoader.loadMarkdowns();
        List<Document> enrichedDocuments = myKeywordEnricher.enrichDocuments(documentList);
        simpleVectorStore.add(enrichedDocuments);
        return simpleVectorStore;
    }
}
