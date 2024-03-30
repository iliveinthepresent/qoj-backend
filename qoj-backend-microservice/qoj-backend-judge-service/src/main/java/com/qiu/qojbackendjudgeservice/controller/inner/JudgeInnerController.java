package com.qiu.qojbackendjudgeservice.controller.inner;

import com.qiu.qojbackendjudgeservice.judge.JudgeService;
import com.qiu.qojbackendserviceclient.service.JudgeFeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/inner")
public class JudgeInnerController implements JudgeFeignClient {

    @Resource
    private JudgeService judgeService;

    /**
     * 判题
     *
     * @param questionSubmitId
     * @return
     */
    @Override
    @PostMapping("/doJudge")
    public Boolean doJudge(@RequestParam("questionSubmitId") long questionSubmitId) {
        return judgeService.doJudge(questionSubmitId);
    }

}
