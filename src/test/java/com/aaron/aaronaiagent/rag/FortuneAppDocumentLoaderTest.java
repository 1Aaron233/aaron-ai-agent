package com.aaron.aaronaiagent.rag;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class FortuneAppDocumentLoaderTest {

    @Resource
    private FortuneAppDocumentLoader fortuneAppDocumentLoader;

    @Test
    void loadMarkdowns() {
        fortuneAppDocumentLoader.loadMarkdowns()
                .forEach(doc -> System.out.println(doc.getMetadata() + " -> " + doc.getText().substring(0, Math.min(50, doc.getText().length()))));
    }
}
