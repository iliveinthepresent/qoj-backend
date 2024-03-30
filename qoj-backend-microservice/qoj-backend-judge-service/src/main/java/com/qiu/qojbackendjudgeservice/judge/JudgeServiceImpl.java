package com.qiu.qojbackendjudgeservice.judge;


import cn.hutool.json.JSONUtil;
import com.qiu.qojbackendcommon.common.ErrorCode;
import com.qiu.qojbackendcommon.constant.QuestionConstant;
import com.qiu.qojbackendcommon.constant.QuestionSubmitConstant;
import com.qiu.qojbackendcommon.exception.BusinessException;
import com.qiu.qojbackendjudgeservice.judge.codesandbox.CodeSandbox;
import com.qiu.qojbackendjudgeservice.judge.codesandbox.CodeSandboxFactory;
import com.qiu.qojbackendjudgeservice.judge.codesandbox.CodeSandboxProxy;
import com.qiu.qojbackendjudgeservice.judge.strategy.JudgeContext;
import com.qiu.qojbackendmodel.codesandbox.ExecuteCodeRequest;
import com.qiu.qojbackendmodel.codesandbox.ExecuteCodeResponse;
import com.qiu.qojbackendmodel.codesandbox.JudgeInfo;
import com.qiu.qojbackendmodel.dto.question.JudgeCase;
import com.qiu.qojbackendmodel.entity.Question;
import com.qiu.qojbackendmodel.entity.QuestionSubmit;
import com.qiu.qojbackendmodel.enums.QuestionSubmitStatusEnum;
import com.qiu.qojbackendserviceclient.service.QuestionFeignClient;
import com.qiu.qojbackendserviceclient.service.QuestionSubmitFeignClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class JudgeServiceImpl implements JudgeService {

    @Resource
    private QuestionFeignClient questionFeignClient;

    @Resource
    private QuestionSubmitFeignClient questionSubmitFeignClient;

    @Resource
    private JudgeManager judgeManager;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Value("${codesandbox.type:example}")
    private String type;


    @Override
    public Boolean doJudge(long questionSubmitId) {
        // 1）传入题目的提交 id，获取到对应的题目、提交信息（包含代码、编程语言等）
        QuestionSubmit questionSubmit = questionSubmitFeignClient.getById(questionSubmitId);
        if (questionSubmit == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "提交信息不存在");
        }
        Long questionId = questionSubmit.getQuestionId();
        Question question = questionFeignClient.getById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题目不存在");
        }
        // 2）如果题目提交状态不为等待中，就不用重复执行了
        if (!questionSubmit.getStatus().equals(QuestionSubmitStatusEnum.WAITING.getValue())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "题目正在判题中");
        }
        // 3）更改判题（题目提交）的状态为 “判题中”，防止重复执行
        String submitStateKey = QuestionSubmitConstant.QUESTION_SUBMIT_STATE_KEY + questionSubmit.getId();
        stringRedisTemplate.opsForValue().set(submitStateKey, QuestionSubmitStatusEnum.RUNNING.getValue().toString(), 5, TimeUnit.MINUTES);
        QuestionSubmit questionSubmitUpdate = new QuestionSubmit();
        questionSubmitUpdate.setId(questionSubmitId);
        questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.RUNNING.getValue());
        boolean update = questionSubmitFeignClient.updateById(questionSubmitUpdate);
        if (!update) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目状态更新错误");
        }
        // 4）调用沙箱，获取到执行结果
        CodeSandbox codeSandbox = CodeSandboxFactory.newInstance(type);
        codeSandbox = new CodeSandboxProxy(codeSandbox);
        String language = questionSubmit.getLanguage();
        String code = questionSubmit.getCode();
        // 获取输入用例
        String judgeCaseStr = question.getJudgeCase();
        List<JudgeCase> judgeCaseList = JSONUtil.toList(judgeCaseStr, JudgeCase.class);
        List<String> inputList = judgeCaseList.stream().map(JudgeCase::getInput).collect(Collectors.toList());
        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
                .code(code)
                .language(language)
                .inputList(inputList)
                .build();
        ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
        List<String> outputList = executeCodeResponse.getOutputList();
        // 5）根据沙箱的执行结果，设置题目的判题状态和信息
        JudgeContext judgeContext = new JudgeContext();
        judgeContext.setJudgeInfo(executeCodeResponse.getJudgeInfo());
        judgeContext.setInputList(inputList);
        judgeContext.setOutputList(outputList);
        judgeContext.setJudgeCaseList(judgeCaseList);
        judgeContext.setQuestion(question);
        judgeContext.setQuestionSubmit(questionSubmit);
        JudgeInfo judgeInfo = judgeManager.doJudge(judgeContext);
        // 6）修改数据库中的判题结果
        questionSubmitUpdate = new QuestionSubmit();
        questionSubmitUpdate.setId(questionSubmitId);
        if ("Wrong Answer".equals(judgeInfo.getMessage()) || "编译错误".equals(judgeInfo.getMessage())) {
            questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.FAILED.getValue());
            stringRedisTemplate.opsForValue().set(submitStateKey, QuestionSubmitStatusEnum.FAILED.getValue().toString(), 5, TimeUnit.MINUTES);

        } else {
            questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.SUCCESS.getValue());
            stringRedisTemplate.opsForValue().set(submitStateKey, QuestionSubmitStatusEnum.SUCCESS.getValue().toString(), 5, TimeUnit.MINUTES);
            String key = QuestionConstant.QUESTION_ACCEPTED_NUMBER + questionId;
            stringRedisTemplate.opsForValue().increment(key);
        }
        questionSubmitUpdate.setJudgeInfo(JSONUtil.toJsonStr(judgeInfo));
        update = questionSubmitFeignClient.updateById(questionSubmitUpdate);
        if (!update) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目状态更新错误");
        }
//        QuestionSubmit questionSubmitResult = questionSubmitService.getById(questionSubmitId);
//        if (questionSubmitResult.getStatus() == 2 && )
        return update;
    }
}
