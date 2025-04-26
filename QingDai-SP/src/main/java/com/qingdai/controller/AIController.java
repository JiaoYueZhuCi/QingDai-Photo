package com.qingdai.controller;

import com.qingdai.service.AIService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI测试接口控制器
 */
@RestController
@RequestMapping("/model")
@Tag(name = "大模型接口", description = "使用AI")
public class AIController {

    @Autowired
    private AIService aiService;

    @PostMapping(value = "/test/generate", produces = MediaType.TEXT_PLAIN_VALUE)
    @Operation(summary = "生成测试用例", description = "根据API描述生成测试用例")
    public String generateTest(
            @Parameter(description = "API描述信息，可以是JSON格式或纯文本描述") 
            @RequestBody String apiDescription) {
        return aiService.generateTest(apiDescription);
    }
} 