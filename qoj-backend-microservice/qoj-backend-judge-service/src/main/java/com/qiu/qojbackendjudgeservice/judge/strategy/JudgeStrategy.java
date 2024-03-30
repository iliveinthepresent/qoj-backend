package com.qiu.qojbackendjudgeservice.judge.strategy;


import com.qiu.qojbackendmodel.codesandbox.JudgeInfo;

public interface JudgeStrategy {

    /**
     * 执行判题
     *
     * @param judgeContext
     * @return
     */
    JudgeInfo doJudge(JudgeContext judgeContext);
}
