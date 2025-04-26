package com.qingdai.service;

/**
 * AI服务接口
 */
public interface AIService {
    
    /**
     * 根据API描述生成测试用例
     * 
     * @param apiDescription API描述信息
     * @return 生成的测试用例
     */
    String generateTest(String apiDescription);
    
} 