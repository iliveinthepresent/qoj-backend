package com.qiu.qojbackendquestionsubmitservice.service;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qiu.qojbackendmodel.dto.questionsubmint.QuestionSubmitAddRequest;
import com.qiu.qojbackendmodel.dto.questionsubmint.QuestionSubmitQueryRequest;
import com.qiu.qojbackendmodel.entity.QuestionSubmit;
import com.qiu.qojbackendmodel.entity.User;
import com.qiu.qojbackendmodel.vo.QuestionSubmitStateVO;
import com.qiu.qojbackendmodel.vo.QuestionSubmitVO;

/**
 * @author 10692
 * @description 针对表【question_submit(题目提交)】的数据库操作Service
 * @createDate 2023-12-11 19:31:25
 */
public interface QuestionSubmitService extends IService<QuestionSubmit> {
    /**
     * 题目提交
     *
     * @param questionSubmitAddRequest
     * @param loginUser
     * @return
     */
    Long doQuestionSubmit(QuestionSubmitAddRequest questionSubmitAddRequest, User loginUser);

    /**
     * 题目提交（内部服务）
     *
     * @param userId
     * @param questionId
     * @return
     */
//    int doQuestionSubmitInner(long userId, long questionId);

    /**
     * 获取查询条件
     *
     * @param questionSubmitQueryRequest
     * @return
     */
    QueryWrapper<QuestionSubmit> getQueryWrapper(QuestionSubmitQueryRequest questionSubmitQueryRequest);


    /**
     * 获取题目封装
     *
     * @param questionSubmit
     * @param loginUser
     * @return
     */
    QuestionSubmitVO getQuestionSubmitVO(QuestionSubmit questionSubmit, User loginUser);

    /**
     * 分页获取题目封装
     *
     * @param questionSubmitPage
     * @param
     * @return
     */
    Page<QuestionSubmitVO> getQuestionSubmitVOPage(Page<QuestionSubmit> questionSubmitPage, User loginUser);

    Integer getQuestionSubmitState(Long questionSubmitId);

    QuestionSubmitStateVO getJudgeInformation(Long questionSubmitId);
}
