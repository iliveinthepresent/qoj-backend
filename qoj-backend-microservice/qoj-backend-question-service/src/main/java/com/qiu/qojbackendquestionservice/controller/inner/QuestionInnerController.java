package com.qiu.qojbackendquestionservice.controller.inner;

import com.qiu.qojbackendmodel.entity.Question;
import com.qiu.qojbackendquestionservice.service.QuestionService;
import com.qiu.qojbackendserviceclient.service.QuestionFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 题目接口
 */
@RestController
@RequestMapping("/inner")
@Slf4j
public class QuestionInnerController implements QuestionFeignClient {

    @Resource
    private QuestionService questionService;



    @Override
    @GetMapping("/get/id")
    public Question getById(@RequestParam("questionId")long questionId) {
        return questionService.getById(questionId);
    }
}
