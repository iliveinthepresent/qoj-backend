package com.qiu.qoj.manager;

import cn.hutool.core.util.StrUtil;
import io.github.briqt.spark4j.SparkClient;
import io.github.briqt.spark4j.constant.SparkApiVersion;
import io.github.briqt.spark4j.exception.SparkException;
import io.github.briqt.spark4j.model.SparkMessage;
import io.github.briqt.spark4j.model.SparkSyncChatResponse;
import io.github.briqt.spark4j.model.request.SparkRequest;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 讯飞星火大模型服务
 */
@Component
public class SparkManager {

    @Resource
    private SparkClient sparkClient;

    /**
     * 发送消息同步等待回复
     *
     * @param systemContent 预设消息
     * @param userContent   用户消息
     * @return 大模型的回答
     */
    public String sendMessageSync(String systemContent, String userContent) {

        // 消息列表，可以在此列表添加历史对话记录
        List<SparkMessage> messages = new ArrayList<>();
        if (StrUtil.isNotBlank(systemContent)) {
            messages.add(SparkMessage.systemContent(systemContent));
        }
        if (StrUtil.isNotBlank(userContent)) {
            messages.add(SparkMessage.userContent(userContent));
        }

        // 构造请求
        SparkRequest sparkRequest = SparkRequest.builder()
                // 消息列表
                .messages(messages)
                // 模型回答的tokens的最大长度,非必传，默认为2048。
                // V1.5取值为[1,4096]
                // V2.0取值为[1,8192]
                // V3.0取值为[1,8192]
                .maxTokens(2048)
                // 核采样阈值。用于决定结果随机性,取值越高随机性越强即相同的问题得到的不同答案的可能性越高 非必传,取值为[0,1],默认为0.5
                .temperature(0.2)
                // 指定请求版本，默认使用最新3.0版本
                .apiVersion(SparkApiVersion.V3_5)
                .build();

        try {
            // 同步调用
            SparkSyncChatResponse chatResponse = sparkClient.chatSync(sparkRequest);
            return chatResponse.getContent();
        } catch (SparkException e) {
            System.out.println("发生异常了：" + e.getMessage());
            return "AI出故障了，请耐心等待修复";
        }
    }
}
