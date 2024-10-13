package com.qiu.qoj.controller;

import com.qiu.qoj.common.BaseResponse;
import com.qiu.qoj.common.ResultUtils;
import com.qiu.qoj.model.vo.QuestionRecommendation;
import com.qiu.qoj.service.AIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/ai")
@Slf4j
public class AIController {

    @Resource
    private AIService aiService;


    /**
     * 根据题目的提交ID和测试用例的序号，分析用户代码中的错误，并且生成修改建议
     *
     * @param questionSubmitId
     * @param index            测试用例的序号（从0开始）
     * @return
     */
    @PostMapping("/analyzeError")
    public BaseResponse<String> analyzeError(@RequestParam Long questionSubmitId, @RequestParam Integer index) {


        String s = aiService.generateAlgorithmProblemModificationSuggestion(questionSubmitId, index);
        return ResultUtils.success(s);
    }


    @PostMapping("/recommendQuestion")
    public BaseResponse<QuestionRecommendation> recommendQuestion(String message, HttpServletRequest httpServletRequest) {
        return ResultUtils.success(aiService.generateQuestionRecommendation(message, httpServletRequest));
    }
}
