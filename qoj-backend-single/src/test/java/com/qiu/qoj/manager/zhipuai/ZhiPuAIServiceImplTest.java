package com.qiu.qoj.manager.zhipuai;

import com.qiu.qoj.manager.AIManage;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

//@RequiredArgsConstructor
@SpringBootTest
class ZhiPuAIServiceImplTest {

    @Resource
    private AIManage aiManage;

    @Test
    void chatForSpeech() {
        System.out.println(aiManage.chatForSpeech("作为一名营销专家，请为智谱开放平台创作一个吸引人的slogan", "4343534"));
    }

    @Test
    void chatForDataAnalysis() {
//        aiManage.chatForDataAnalysis()
    }

    @Test
    void chatWithKnowledgeBase() {
        System.out.println(aiManage.chatWithKnowledgeBase("给我推荐一些模拟的简单题目", "4543543678678", "1845342976004509696"));
    }
}