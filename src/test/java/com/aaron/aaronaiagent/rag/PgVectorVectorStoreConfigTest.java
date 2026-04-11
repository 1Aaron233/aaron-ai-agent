package com.aaron.aaronaiagent.rag;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

@SpringBootTest
class PgVectorVectorStoreConfigTest {

    @Resource
    private VectorStore pgVectorVectorStore;

    @Test
    void pgVectorVectorStore() {
        List<Document> documents = List.of(
                new Document("Aaron 的发展路线有什么？有他自己吧，跟着命运吧", Map.of("meta1", "meta1")),
                new Document("程序员 Aaron 的项目分享"),
                new Document("Aaron 这小伙子比较信命", Map.of("meta2", "meta2")));
        // 添加文档
        pgVectorVectorStore.add(documents);
        // 相似度查询
        List<Document> results = pgVectorVectorStore.similaritySearch(SearchRequest.builder().query("怎么学算命啊").topK(3).build());
        Assertions.assertNotNull(results);
        System.out.println("查询到 " + results.size() + " 条结果：");
        results.forEach(doc -> System.out.println("  - " + doc.getText()));
    }
}
