package com.qiu.qojbackendquestionsubmitservice;

import com.qiu.qojbackendquestionsubmitservice.message.MessageProducer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class MyMessageProducerTest {

    @Resource
    private MessageProducer MessageProducer;

    @Test
    void sendMessage() {
        MessageProducer.sendMessage("code_exchange", "my_routingKey", "你好呀");
    }
}