package com.qiu.qoj.service;

import com.qiu.qoj.model.vo.QuestionRecommendation;

import javax.servlet.http.HttpServletRequest;

public interface AIService {


    String generateAlgorithmProblemModificationSuggestion(Long questionSubmitId, Integer index);

    QuestionRecommendation generateQuestionRecommendation(String message, HttpServletRequest httpServletRequest);
}
