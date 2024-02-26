package com.qiu.qoj.judge;

import com.qiu.qoj.model.entity.QuestionSubmit;

/**
 * 判题服务
 */
public interface JudgeService {

    /**
     * 判题
     * @param questionSubmitId
     * @return
     */
    Boolean doJudge(long questionSubmitId);
}
