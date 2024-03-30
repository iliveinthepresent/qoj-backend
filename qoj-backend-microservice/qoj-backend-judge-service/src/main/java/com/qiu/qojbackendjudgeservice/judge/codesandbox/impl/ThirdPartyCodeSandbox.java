package com.qiu.qojbackendjudgeservice.judge.codesandbox.impl;


import com.qiu.qojbackendjudgeservice.judge.codesandbox.CodeSandbox;
import com.qiu.qojbackendmodel.codesandbox.ExecuteCodeRequest;
import com.qiu.qojbackendmodel.codesandbox.ExecuteCodeResponse;

/**
 * 第三方代码沙箱（调用网上现成的代码沙箱）
 */
public class ThirdPartyCodeSandbox implements CodeSandbox {
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        System.out.println("第三方代码沙箱");
        return null;
    }
}
