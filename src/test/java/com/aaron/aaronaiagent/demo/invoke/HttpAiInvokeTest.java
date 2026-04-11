package com.aaron.aaronaiagent.demo.invoke;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * HTTP 方式调用 AI 测试
 * 注意：需要在 TestApiKey 中配置有效的 API Key
 */
public class HttpAiInvokeTest {

    @Test
    void testHttpInvoke() {
        // HttpAiInvoke 的逻辑封装在 main() 里，这里直接调用验证不抛异常
        Assertions.assertDoesNotThrow(() -> HttpAiInvoke.main(new String[]{}));
    }
}
