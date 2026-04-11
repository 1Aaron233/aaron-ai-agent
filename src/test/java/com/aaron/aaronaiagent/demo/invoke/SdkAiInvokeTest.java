package com.aaron.aaronaiagent.demo.invoke;

import com.alibaba.dashscope.aigc.generation.GenerationResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 阿里云灵积 SDK 调用测试
 * 注意：需要在 TestApiKey 中配置有效的 API Key
 */
public class SdkAiInvokeTest {

    @Test
    void testCallWithMessage() throws Exception {
        GenerationResult result = SdkAiInvoke.callWithMessage();
        Assertions.assertNotNull(result);
        String content = result.getOutput().getChoices().get(0).getMessage().getContent();
        System.out.println("SDK 调用结果：" + content);
    }
}
