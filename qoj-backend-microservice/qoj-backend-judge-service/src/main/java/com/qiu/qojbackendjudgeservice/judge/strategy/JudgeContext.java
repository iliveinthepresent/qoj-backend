package com.qiu.qojbackendjudgeservice.judge.strategy;

import com.qiu.qojbackendmodel.codesandbox.JudgeInfo;
import com.qiu.qojbackendmodel.dto.question.JudgeCase;
import com.qiu.qojbackendmodel.entity.Question;
import com.qiu.qojbackendmodel.entity.QuestionSubmit;
import lombok.Data;

import java.util.List;

/**
 * 上下文（用于定义在策略中传递的参数）
 */
@Data
public class JudgeContext {

    private JudgeInfo judgeInfo;

    private List<String> inputList;

    private List<String> outputList;

    private List<JudgeCase> judgeCaseList;

    private Question question;

    private QuestionSubmit questionSubmit;

}
