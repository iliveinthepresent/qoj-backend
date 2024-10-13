package com.qiu.qoj.service.impl;

import com.qiu.qoj.service.AIService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class AIServiceImplTest {

    @Resource
    private AIService aiService;
    @Test
    void generateAlgorithmProblemModificationSuggestion() {
        System.out.println(aiService.generateAlgorithmProblemModificationSuggestion(1845292897688252418L, 0));
    }

    @Test
    void generateQuestionRecommendation() {
        System.out.println(aiService.generateQuestionRecommendation("我想学习模拟", null));
    }
}