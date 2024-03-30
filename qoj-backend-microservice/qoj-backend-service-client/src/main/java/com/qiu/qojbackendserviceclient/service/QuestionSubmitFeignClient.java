package com.qiu.qojbackendserviceclient.service;


import com.qiu.qojbackendmodel.entity.QuestionSubmit;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author 10692
 * @description 针对表【question_submit(题目提交)】的数据库操作Service
 * @createDate 2023-12-11 19:31:25
 */
@FeignClient(name = "qoj-backend-question-submit-service", path = "/api/question_submit/inner")
public interface QuestionSubmitFeignClient {
    @PostMapping("/update")
    boolean updateById(@RequestBody QuestionSubmit questionSubmit);

    @GetMapping("/get/id")
    QuestionSubmit getById(@RequestParam("questionSubmitId") long questionSubmitId);
}
