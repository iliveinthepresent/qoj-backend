package com.qiu.qojbackendjudgeservice.judge;


import com.qiu.qojbackendjudgeservice.judge.strategy.DefaultJudgeStrategy;
import com.qiu.qojbackendjudgeservice.judge.strategy.JavaLanguageJudgeStrategy;
import com.qiu.qojbackendjudgeservice.judge.strategy.JudgeContext;
import com.qiu.qojbackendjudgeservice.judge.strategy.JudgeStrategy;
import com.qiu.qojbackendmodel.codesandbox.JudgeInfo;
import com.qiu.qojbackendmodel.entity.QuestionSubmit;
import org.springframework.stereotype.Service;

/**
 * 判题管理（简化调用）
 */
@Service
public class JudgeManager {

    /**
     * 执行判题
     *
     * @param judgeContext
     * @return
     */
    JudgeInfo doJudge(JudgeContext judgeContext) {
        QuestionSubmit questionSubmit = judgeContext.getQuestionSubmit();
        String language = questionSubmit.getLanguage();
        JudgeStrategy judgeStrategy = new DefaultJudgeStrategy();
        if ("java".equals(language)) {
            judgeStrategy = new JavaLanguageJudgeStrategy();
        }
        return judgeStrategy.doJudge(judgeContext);
    }

}
