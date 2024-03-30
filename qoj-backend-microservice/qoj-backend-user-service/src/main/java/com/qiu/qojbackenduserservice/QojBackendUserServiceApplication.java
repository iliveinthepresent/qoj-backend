package com.qiu.qojbackenduserservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.qiu.qojbackenduserservice.mapper")
@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@ComponentScan("com.qiu")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.qiu.qojbackendserviceclient.service"})
public class QojBackendUserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(QojBackendUserServiceApplication.class, args);
    }

}
