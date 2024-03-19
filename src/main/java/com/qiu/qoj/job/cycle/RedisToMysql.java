package com.qiu.qoj.job.cycle;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qiu.qoj.constant.QuestionConstant;
import com.qiu.qoj.model.entity.Question;
import com.qiu.qoj.service.QuestionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;


@Component
@Slf4j
public class RedisToMysql {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private QuestionService questionService;

    /**
     * 每分钟执行一次
     */
    @Scheduled(fixedRate = 60 * 1000 * 120)
    public void run() {
        QueryWrapper<Question> QueryWrapper = new QueryWrapper<>();
        QueryWrapper.select("id");
        List<Question> list = questionService.list(QueryWrapper);
        Question questionTemp = new Question();
        for (Question question : list) {
            questionTemp = new Question();
            questionTemp.setId(question.getId());
            Double score = stringRedisTemplate.opsForZSet().score(QuestionConstant.QUESTION_SUBMIT_NUMBER, question.getId().toString());
            if (score == null) {
                continue;
            }
            int sumbit = score.intValue();
            questionTemp.setSubmitNum(sumbit);
            // todo 其他内容的持久化
            questionService.updateById(questionTemp);
        }
    }
}
