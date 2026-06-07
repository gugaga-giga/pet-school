package com.petschool.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * AI模块配置类，读取application.yml中的ai配置
 */
@Configuration
@ConfigurationProperties(prefix = "ai")
public class AiConfig {

    private String apiKey;
    private String baseUrl;
    private String modelName;
    private String ragBaseUrl;
    private int maxContextLength = 4000;

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }
    public String getRagBaseUrl() { return ragBaseUrl; }
    public void setRagBaseUrl(String ragBaseUrl) { this.ragBaseUrl = ragBaseUrl; }
    public int getMaxContextLength() { return maxContextLength; }
    public void setMaxContextLength(int maxContextLength) { this.maxContextLength = maxContextLength; }

    /**
     * 用于调用RAG服务的RestTemplate
     */
    @Bean
    public RestTemplate ragRestTemplate() {
        return new RestTemplate();
    }
}
