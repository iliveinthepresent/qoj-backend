package com.qiu.qojbackendquestionsubmitservice.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiu.qojbackendcommon.common.BaseResponse;
import com.qiu.qojbackendcommon.common.ErrorCode;
import com.qiu.qojbackendcommon.common.ResultUtils;
import com.qiu.qojbackendcommon.exception.BusinessException;
import com.qiu.qojbackendmodel.dto.questionsubmint.QuestionSubmitAddRequest;
import com.qiu.qojbackendmodel.dto.questionsubmint.QuestionSubmitQueryRequest;
import com.qiu.qojbackendmodel.entity.QuestionSubmit;
import com.qiu.qojbackendmodel.entity.User;
import com.qiu.qojbackendmodel.vo.QuestionSubmitStateVO;
import com.qiu.qojbackendmodel.vo.QuestionSubmitVO;
import com.qiu.qojbackendquestionsubmitservice.service.QuestionSubmitService;
import com.qiu.qojbackendserviceclient.service.UserFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 题目提交接口
 */
@RestController
@RequestMapping
@Slf4j
public class QuestionSubmitController {

    @Resource
    private QuestionSubmitService questionSubmitService;

    @Resource
    private UserFeignClient userFeignClient;

    /**
     * 提交题目
     *
     * @param questionSubmitAddRequest
     * @param request
     * @return resultNum
     */
    @PostMapping("/")
    public BaseResponse<Long> doQuestionSubmit(@RequestBody QuestionSubmitAddRequest questionSubmitAddRequest,
                                               HttpServletRequest request) {
        if (questionSubmitAddRequest == null || questionSubmitAddRequest.getQuestionId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        final User loginUser = userFeignClient.getLoginUser(request);
        Long questionSubmitId = questionSubmitService.doQuestionSubmit(questionSubmitAddRequest, loginUser);
//        QuestionSubmitStateVO questionSubmitStateVO = new QuestionSubmitStateVO();
//        questionSubmitStateVO.setStatus(questionSubmit.getStatus());
//        questionSubmitStateVO.setJudgeInfo(JSONUtil.parse(questionSubmit.getJudgeInfo()).toBean(JudgeInfo.class));
        return ResultUtils.success(questionSubmitId);
    }

    /**
     * 分页获取题目提交列表（仅本人和管理员能看见自己提交的代码）
     *
     * @param questionSubmitQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<QuestionSubmitVO>> listQuestionSubmitByPage(@RequestBody QuestionSubmitQueryRequest questionSubmitQueryRequest,
                                                                         HttpServletRequest request) {
        long current = questionSubmitQueryRequest.getCurrent();
        long size = questionSubmitQueryRequest.getPageSize();

        // 从数据库中获取原始的分页数据
        Page<QuestionSubmit> questionSubmitPage = questionSubmitService.page(new Page<>(current, size),
                questionSubmitService.getQueryWrapper(questionSubmitQueryRequest));

        final User loginUser = userFeignClient.getLoginUser(request);
        // 脱敏
        return ResultUtils.success(questionSubmitService.getQuestionSubmitVOPage(questionSubmitPage, loginUser));
    }

    @GetMapping("/state")
    public Integer getQuestionSubmitState(@RequestParam Long questionSubmitId) {
        return questionSubmitService.getQuestionSubmitState(questionSubmitId);
    }

    @GetMapping("/judgeInformation")
    public BaseResponse<QuestionSubmitStateVO> getJudgeInformation(@RequestParam Long questionSubmitId) {
        QuestionSubmitStateVO judgeInformation = questionSubmitService.getJudgeInformation(questionSubmitId);
        return ResultUtils.success(judgeInformation);
    }


}
