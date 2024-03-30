package com.qiu.qoj.controller;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.qiu.qoj.common.BaseResponse;
import com.qiu.qoj.common.ErrorCode;
import com.qiu.qoj.common.ResultUtils;
import com.qiu.qoj.exception.BusinessException;
import com.qiu.qoj.exception.ThrowUtils;
import com.qiu.qoj.manager.SparkManager;
import com.qiu.qoj.model.entity.Question;
import com.qiu.qoj.model.entity.QuestionSubmit;
import com.qiu.qoj.model.entity.User;
import com.qiu.qoj.service.QuestionService;
import com.qiu.qoj.service.QuestionSubmitService;
import com.qiu.qoj.service.UserService;
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
    private SparkManager sparkManager;

    @Resource
    private UserService userService;


    @Resource
    private QuestionService questionService;

    @Resource
    private QuestionSubmitService questionSubmitService;

    /**
     * 分析用户出错的代码
     * @param questionId
     * @return 分析结果
     */
    @PostMapping("/analyzeError")
    public BaseResponse<String> analyzeError(@RequestParam Long questionId, HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        Long userId = loginUser.getId();

        // 获取题目信息
        Question question = questionService.getById(questionId);
        ThrowUtils.throwIf(question == null, new BusinessException(ErrorCode.PARAMS_ERROR, "题目ID不存在"));
        String questionContent = question.getContent();

        // 获取用户的错误代码
        LambdaQueryChainWrapper<QuestionSubmit> questionSubmitLambdaQueryChainWrapper = questionSubmitService.lambdaQuery()
                .eq(QuestionSubmit::getUserId, userId)
                .eq(QuestionSubmit::getQuestionId, questionId)
                .orderByDesc(QuestionSubmit::getId);

        QuestionSubmit questionSubmit = questionSubmitService.getOne(questionSubmitLambdaQueryChainWrapper);
        String errorCode = questionSubmit.getCode();
        String language = questionSubmit.getLanguage();
//        String systemContent = "我"
//        sparkManager.sendMessageSync()
        return ResultUtils.success("");
    }
}
