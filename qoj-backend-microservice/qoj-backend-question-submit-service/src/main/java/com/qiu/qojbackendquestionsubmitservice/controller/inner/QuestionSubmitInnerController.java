package com.qiu.qojbackendquestionsubmitservice.controller.inner;

import com.qiu.qojbackendmodel.entity.QuestionSubmit;
import com.qiu.qojbackendquestionsubmitservice.service.QuestionSubmitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 题目提交接口
 */
@RestController
@RequestMapping("/inner")
@Slf4j
public class QuestionSubmitInnerController {

    @Resource
    private QuestionSubmitService questionSubmitService;


    @PostMapping("/update")
    boolean updateById(@RequestBody QuestionSubmit questionSubmit) {
        return questionSubmitService.updateById(questionSubmit);
    }

    @GetMapping("/get/id")
    QuestionSubmit getById(@RequestParam("questionSubmitId") long questionSubmitId) {
        return questionSubmitService.getById(questionSubmitId);
    }


}
