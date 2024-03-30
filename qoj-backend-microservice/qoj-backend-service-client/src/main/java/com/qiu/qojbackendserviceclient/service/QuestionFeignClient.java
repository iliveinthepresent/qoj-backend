package com.qiu.qojbackendserviceclient.service;



import com.qiu.qojbackendmodel.entity.Question;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


/**
 * @author 10692
 * @description 针对表【question(题目)】的数据库操作Service
 * @createDate 2023-12-11 19:30:57
 */
@FeignClient(name = "qoj-backend-question-service", path = "/api/question/inner")
public interface QuestionFeignClient {
    @GetMapping("/get/id")
    Question getById(@RequestParam("questionId") long questionId);
}
