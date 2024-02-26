package com.qiu.qoj.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.qiu.qoj.common.BaseResponse;
import com.qiu.qoj.common.DeleteRequest;
import com.qiu.qoj.common.ErrorCode;
import com.qiu.qoj.common.ResultUtils;
import com.qiu.qoj.constant.QuestionConstant;
import com.qiu.qoj.constant.QuestionSolvingConstant;
import com.qiu.qoj.exception.BusinessException;
import com.qiu.qoj.exception.ThrowUtils;
import com.qiu.qoj.model.dto.questionsolving.QuestionSolvingAddRequest;
import com.qiu.qoj.model.dto.questionsolving.QuestionSolvingQueryRequest;
import com.qiu.qoj.model.dto.questionsolving.QuestionSolvingUpdateRequest;
import com.qiu.qoj.model.entity.QuestionSolving;
import com.qiu.qoj.model.entity.User;
import com.qiu.qoj.model.vo.QuestionSolvingPageVO;
import com.qiu.qoj.service.QuestionSolvingService;
import com.qiu.qoj.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 题目接口
 */
@RestController
@RequestMapping("/questionSolving")
@Slf4j
public class QuestionSolvingController {


    @Resource
    private QuestionSolvingService questionSolvingService;

    @Resource
    private UserService userService;

    @Resource StringRedisTemplate stringRedisTemplate;

    private final static Gson GSON = new Gson();

    // region 增删改查

    /**
     * 创建
     *
     * @param questionSolvingAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addQuestionSolving(@RequestBody QuestionSolvingAddRequest questionSolvingAddRequest, HttpServletRequest request) {
        if (questionSolvingAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QuestionSolving questionSolving = new QuestionSolving();
        BeanUtils.copyProperties(questionSolvingAddRequest, questionSolving);

        // todo 无效参数校验

        User loginUser = userService.getLoginUser(request);
        questionSolving.setUserId(loginUser.getId());


        boolean result = questionSolvingService.save(questionSolving);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newQuestionSolvingId = questionSolving.getId();
        return ResultUtils.success(newQuestionSolvingId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteQuestionSolving(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        QuestionSolving oldQuestionSolving = questionSolvingService.getById(id);
        ThrowUtils.throwIf(oldQuestionSolving == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldQuestionSolving.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = questionSolvingService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新
     *
     * @param questionSolvingUpdateRequest
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateQuestionSolving(@RequestBody QuestionSolvingUpdateRequest questionSolvingUpdateRequest, HttpServletRequest httpServletRequest) {
        if (questionSolvingUpdateRequest == null || questionSolvingUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }



        // todo 参数校验

        // 只有本人或管理员可修改
        long id = questionSolvingUpdateRequest.getId();
        QuestionSolving oldQuestionSolving = questionSolvingService.getById(id);
        ThrowUtils.throwIf(oldQuestionSolving == null, ErrorCode.NOT_FOUND_ERROR);
        Long oldQuestionSolvingUserId = oldQuestionSolving.getUserId();
        User loginUser = userService.getLoginUser(httpServletRequest);
        Long newQuestionSolvingUserId = loginUser.getId();
        if(!oldQuestionSolvingUserId.equals(newQuestionSolvingUserId) &&!userService.isAdmin(httpServletRequest)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        QuestionSolving questionSolving = new QuestionSolving();
        BeanUtils.copyProperties(questionSolvingUpdateRequest, questionSolving);

        boolean result = questionSolvingService.updateById(questionSolving);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param
     * @return
     */

    @PostMapping("/get")
    public BaseResponse<QuestionSolving> getQuestionSolving(@RequestBody QuestionSolvingQueryRequest questionSolvingQueryRequest, HttpServletRequest request) {
        if (questionSolvingQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<QuestionSolving> queryWrapper = questionSolvingService.getQueryWrapper(questionSolvingQueryRequest);
        if (queryWrapper == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        QuestionSolving res = questionSolvingService.getOne(queryWrapper);
        if(res == null) {
            return ResultUtils.error(ErrorCode.NOT_FOUND_ERROR);
        }
        String key = QuestionSolvingConstant.QUESTION_SOLVING_PAGE_VIEW_KEY + questionSolvingQueryRequest.getId();
        stringRedisTemplate.opsForValue().increment(key);
        res.setSupported(questionSolvingService.isSupported(res.getId(), request));
        return ResultUtils.success(res);
    }

//    @GetMapping("/user")
//    public BaseResponse<QuestionSolving> getQuestionSolvingByUser(long questionId, HttpServletRequest request) {
//        if (questionId <= 0) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR);
//        }
//        QuestionSolving questionSolving = questionSolvingService.getUserQuestionSolving(questionId, request);
//        if (questionSolving == null) {
//            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
//        }
//
//        return ResultUtils.success(questionSolving);
//    }

    /**
     * 赞同题解
     * @param id
     * @param httpServletRequest
     * @return
     */
    @PutMapping("/like/{id}")
    public BaseResponse supportQuestionSolving(@PathVariable("id") Long id, HttpServletRequest httpServletRequest) {
        Boolean success = questionSolvingService.likeQuestionSolving(id, httpServletRequest);
        return ResultUtils.success(null);
    }

    /**
     * 分页获取题解列表
     *
     * @param questionSolvingQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<QuestionSolvingPageVO>> listQuestionSolvingPageVOByPage(@RequestBody QuestionSolvingQueryRequest questionSolvingQueryRequest,
                                                                                     HttpServletRequest request) {
        long current = questionSolvingQueryRequest.getCurrent();
        long size = questionSolvingQueryRequest.getPageSize();
        Page<QuestionSolving> questionSolvingPage = questionSolvingService.page(new Page<>(current, size), questionSolvingService.getQueryWrapper(questionSolvingQueryRequest)
        );
        Page<QuestionSolvingPageVO> questionSolvingPageVO = questionSolvingService.getQuestionSolvingPageVO(questionSolvingPage, request);
        return ResultUtils.success(questionSolvingPageVO);
    }

//
//    /**
//     * 分页获取当前用户创建的资源列表
//     *
//     * @param questionSolvingQueryRequest
//     * @param request
//     * @return
//     */
//    @PostMapping("/my/list/page/vo")
//    public BaseResponse<Page<QuestionSolvingVO>> listMyQuestionSolvingVOByPage(@RequestBody QuestionSolvingQueryRequest questionSolvingQueryRequest,
//                                                                 HttpServletRequest request) {
//        if (questionSolvingQueryRequest == null) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR);
//        }
//        User loginUser = userService.getLoginUser(request);
//        questionSolvingQueryRequest.setUserId(loginUser.getId());
//        long current = questionSolvingQueryRequest.getCurrent();
//        long size = questionSolvingQueryRequest.getPageSize();
//        // 限制爬虫
//        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
//        Page<QuestionSolving> questionSolvingPage = questionSolvingService.page(new Page<>(current, size),
//                questionSolvingService.getQueryWrapper(questionSolvingQueryRequest));
//        return ResultUtils.success(questionSolvingService.getQuestionSolvingVOPage(questionSolvingPage, request));
//
//
//    /**
//     * 编辑（用户）
//     *
//     * @param questionSolvingEditRequest
//     * @param request
//     * @return
//     */
//    @PostMapping("/edit")
//    public BaseResponse<Boolean> editQuestionSolving(@RequestBody QuestionSolvingEditRequest questionSolvingEditRequest, HttpServletRequest request) {
//        if (questionSolvingEditRequest == null || questionSolvingEditRequest.getId() <= 0) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR);
//        }
//        QuestionSolving questionSolving = new QuestionSolving();
//        BeanUtils.copyProperties(questionSolvingEditRequest, questionSolving);
//        List<String> tags = questionSolvingEditRequest.getTags();
//        if (tags != null) {
//            questionSolving.setTags(GSON.toJson(tags));
//        }
//        List<JudgeCase> judgeCase = questionSolvingEditRequest.getJudgeCase();
//        if(judgeCase != null) {
//            questionSolving.setJudgeCase(GSON.toJson(judgeCase));
//        }
//        JudgeConfig judgeConfig = questionSolvingEditRequest.getJudgeConfig();
//        if(judgeConfig!= null) {
//            questionSolving.setJudgeConfig(GSON.toJson(judgeConfig));
//        }
//        // 参数校验
//        questionSolvingService.validQuestionSolving(questionSolving, false);
//        User loginUser = userService.getLoginUser(request);
//        long id = questionSolvingEditRequest.getId();
//        // 判断是否存在
//        QuestionSolving oldQuestionSolving = questionSolvingService.getById(id);
//        ThrowUtils.throwIf(oldQuestionSolving == null, ErrorCode.NOT_FOUND_ERROR);
//        // 仅本人或管理员可编辑
//        if (!oldQuestionSolving.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
//            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
//        }
//        boolean result = questionSolvingService.updateById(questionSolving);
//        return ResultUtils.success(result);
//    }

}
