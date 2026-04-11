package com.aaron.aaronaiagent.demo.rag;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.rag.Query;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class MultiQueryExpanderDemoTest {

    @Resource
    private MultiQueryExpanderDemo multiQueryExpanderDemo;

    @Test
    void expand() {
        List<Query> queries = multiQueryExpanderDemo.expand("我是程序员 Aaron ？！你是谁？有没有什么自主意识？");
        Assertions.assertNotNull(queries);
        queries.forEach(q -> System.out.println("扩展查询：" + q.text()));
    }
}
