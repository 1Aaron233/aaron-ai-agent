package com.aaron.aaronaiagent.demo.invoke;

/**
 * 仅用于测试获取 API Key
 */
public interface TestApiKey {

    String API_KEY = System.getenv().getOrDefault("DASHSCOPE_API_KEY", "your-api-key-here");
}
