package com.qiu.qoj.job.cycle;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qiu.qoj.constant.CosConstant;
import com.qiu.qoj.constant.QuestionConstant;
import com.qiu.qoj.constant.UserConstant;
import com.qiu.qoj.manager.CosManager;
import com.qiu.qoj.model.entity.Question;
import com.qiu.qoj.service.QuestionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@Component
@Slf4j
public class cleanUselessImagesInCos {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private CosManager cosManager;
    /**
     * 每天0点执行一次
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void cleanUselessImagesInCos() {
        Set<String> difference = stringRedisTemplate.opsForSet().difference(UserConstant.USER_AVATAR_SET, UserConstant.USER_AVATAR_DB_SET);
        if (CollUtil.isNotEmpty(difference)) {
            cosManager.deleteObjects(CosConstant.BUCKET_NAME, new ArrayList<>(difference));
        }
    }
}
