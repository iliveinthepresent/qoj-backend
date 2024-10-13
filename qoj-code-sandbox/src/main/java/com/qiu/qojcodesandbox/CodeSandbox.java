package com.qiu.qojcodesandbox;

import com.qiu.qojcodesandbox.model.ExecuteCodeRequest;
import com.qiu.qojcodesandbox.model.ExecuteCodeResponse;

import java.io.IOException;

/**
 * 代码沙箱接口定义
 */
public interface CodeSandbox {

    /**
     * 执行代码
     *
     * @param executeCodeRequest
     * @return
     */
    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) throws IOException, InterruptedException;
}
