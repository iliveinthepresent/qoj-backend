package com.qiu.qojbackendjudgeservice.judge.codesandbox;


import com.qiu.qojbackendmodel.codesandbox.ExecuteCodeRequest;
import com.qiu.qojbackendmodel.codesandbox.ExecuteCodeResponse;

public interface CodeSandbox {

    /**
     * 执行代码
     *
     * @param executeCodeRequest
     * @return
     */
    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest);
}
