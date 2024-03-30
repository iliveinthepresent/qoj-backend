package com.qiu.qojbackendjudgeservice.judge.codesandbox.impl;


import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.qiu.qojbackendcommon.common.ErrorCode;
import com.qiu.qojbackendcommon.exception.BusinessException;
import com.qiu.qojbackendjudgeservice.judge.codesandbox.CodeSandbox;
import com.qiu.qojbackendmodel.codesandbox.ExecuteCodeRequest;
import com.qiu.qojbackendmodel.codesandbox.ExecuteCodeResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;

/**
 * 远程代码沙箱（实际调用接口的沙箱）
 */
public class RemoteCodeSandbox implements CodeSandbox {

    // 定义鉴权请求头和密钥
    private static final String AUTH_REQUEST_HEADER = "auth";

    private static final String AUTH_REQUEST_SECRET = "secretKey";

    @Value("${codesandbox.remote-url}")
    private String remoteURL = "http://106.15.195.87:8090/executeCode";


    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        System.out.println("远程代码沙箱");
        String url = remoteURL;
        String json = JSONUtil.toJsonStr(executeCodeRequest);
        String responseStr = HttpUtil.createPost(url)
                .header(AUTH_REQUEST_HEADER, AUTH_REQUEST_SECRET)
                .body(json)
                .execute()
                .body();
        if (StringUtils.isBlank(responseStr)) {
            throw new BusinessException(ErrorCode.API_REQUEST_ERROR, "executeCode remoteSandbox error, message = " + responseStr);
        }
        return JSONUtil.toBean(responseStr, ExecuteCodeResponse.class);
    }
}