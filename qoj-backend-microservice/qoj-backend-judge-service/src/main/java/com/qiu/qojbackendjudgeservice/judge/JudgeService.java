package com.qiu.qojbackendjudgeservice.judge;

/**
 * 判题服务
 */
public interface JudgeService {

    /**
     * 判题
     *
     * @param questionSubmitId
     * @return
     */
    Boolean doJudge(long questionSubmitId);
}
