package com.qiu.qojbackendquestionsubmitservice.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qiu.qojbackendcommon.common.ErrorCode;
import com.qiu.qojbackendcommon.constant.CommonConstant;
import com.qiu.qojbackendcommon.constant.QuestionConstant;
import com.qiu.qojbackendcommon.constant.QuestionSubmitConstant;
import com.qiu.qojbackendcommon.exception.BusinessException;
import com.qiu.qojbackendcommon.utils.SqlUtils;
import com.qiu.qojbackendmodel.codesandbox.JudgeInfo;
import com.qiu.qojbackendmodel.dto.questionsubmint.QuestionSubmitAddRequest;
import com.qiu.qojbackendmodel.dto.questionsubmint.QuestionSubmitQueryRequest;
import com.qiu.qojbackendmodel.entity.Question;
import com.qiu.qojbackendmodel.entity.QuestionSubmit;
import com.qiu.qojbackendmodel.entity.User;
import com.qiu.qojbackendmodel.enums.QuestionSubmitLanguageEnum;
import com.qiu.qojbackendmodel.enums.QuestionSubmitStatusEnum;
import com.qiu.qojbackendmodel.vo.QuestionSubmitStateVO;
import com.qiu.qojbackendmodel.vo.QuestionSubmitVO;
import com.qiu.qojbackendquestionsubmitservice.mapper.QuestionSubmitMapper;
import com.qiu.qojbackendquestionsubmitservice.message.MessageProducer;
import com.qiu.qojbackendquestionsubmitservice.service.QuestionSubmitService;
import com.qiu.qojbackendserviceclient.service.JudgeFeignClient;
import com.qiu.qojbackendserviceclient.service.QuestionFeignClient;
import com.qiu.qojbackendserviceclient.service.UserFeignClient;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author 10692
 * @description 针对表【question_submit(题目提交)】的数据库操作Service实现
 * @createDate 2023-12-11 19:31:25
 */
@Service
public class QuestionSubmitServiceImpl extends ServiceImpl<QuestionSubmitMapper, QuestionSubmit>
        implements QuestionSubmitService {

    @Resource
    private QuestionFeignClient questionFeignClient;

    @Resource
    private UserFeignClient userFeignClient;

    @Resource
    @Lazy
    private JudgeFeignClient judgeFeignClient;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private MessageProducer messageProducer;

    private static final ExecutorService QUESTION_SUBMIT_EXECUTOR = Executors.newSingleThreadExecutor();

//    @PostConstruct
//    private void init() {
//        QUESTION_SUBMIT_EXECUTOR.submit(new QUESTIONSUBMITHandler());
//    }
//
//    private class QUESTIONSUBMITHandler implements Runnable {
//
//        @Override
//        public void run() {
//            while (true) {
//                try {
//                    // 1.获取消息队列中的题目提交信息 XREADGROUP GROUP g1 c1 COUNT 1 BLOCK 2000 STREAMS s1 >
//                    List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
//                            Consumer.from("g1", "c1"),
//                            StreamReadOptions.empty().count(1).block(Duration.ofSeconds(2)),
//                            StreamOffset.create("stream.questionSubmit", ReadOffset.lastConsumed())
//                    );
//                    // 2.判断题目提交信息是否为空
//                    if (list == null || list.isEmpty()) {
//                        // 如果为null，说明没有消息，继续下一次循环
//                        continue;
//                    }
//                    // 解析数据
//                    MapRecord<String, Object, Object> record = list.get(0);
//
//                    Map<Object, Object> value = record.getValue();
//                    Long questionSubmitId = Long.parseLong(value.get("questionSubmitId").toString());
////                    // 3.进行处理
//                    judgeFeignClient.doJudge(questionSubmitId);
//
//                    // 4.确认消息 XACK
//                    stringRedisTemplate.opsForStream().acknowledge("s1", "g1", record.getId());
//                } catch (Exception e) {
//                    log.error("处理订单异常", e);
////                    handlePendingList();
//                }
//            }
//        }
//
//        private void handlePendingList() {
//            while (true) {
//                try {
//                    // 1.获取pending-list中的题目提交信息 XREADGROUP GROUP g1 c1 COUNT 1 BLOCK 2000 STREAMS s1 0
//                    List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
//                            Consumer.from("g1", "c1"),
//                            StreamReadOptions.empty().count(1),
//                            StreamOffset.create("stream.questionSubmit", ReadOffset.from("0"))
//                    );
//                    // 2.判断题目提交信息是否为空
//                    if (list == null || list.isEmpty()) {
//                        // 如果为null，说明没有异常消息，结束循环
//                        break;
//                    }
//                    // 解析数据
//                    MapRecord<String, Object, Object> record = list.get(0);
//
//                    Map<Object, Object> value = record.getValue();
//                    Long questionSubmitId = Long.parseLong(value.get("questionSubmitId").toString());
////                    // 3.进行处理
//                    judgeFeignClient.doJudge(questionSubmitId);
//
//                    // 4.确认消息 XACK
//                    stringRedisTemplate.opsForStream().acknowledge("s1", "g1", record.getId());
//                } catch (Exception e) {
//                    log.error("处理订单异常", e);
//                }
//            }
//        }
//    }

    /**
     * 提交题目
     *
     * @param questionSubmitAddRequest
     * @param loginUser
     * @return 提交记录的id
     */
    @Override
    public Long doQuestionSubmit(QuestionSubmitAddRequest questionSubmitAddRequest, User loginUser) {
        // 校验编程语言是否合法
        String language = questionSubmitAddRequest.getLanguage();
        QuestionSubmitLanguageEnum languageEnum = QuestionSubmitLanguageEnum.getEnumByValue(language);
        if (languageEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "编程语言错误");
        }
        long questionId = questionSubmitAddRequest.getQuestionId();
        // 判断实体是否存在，根据类别获取实体
        Question question = questionFeignClient.getById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 是否已提交题目
        long userId = loginUser.getId();
        // 每个用户串行提交题目
        QuestionSubmit questionSubmit = new QuestionSubmit();
        questionSubmit.setUserId(userId);
        questionSubmit.setQuestionId(questionId);
        questionSubmit.setCode(questionSubmitAddRequest.getCode());
        questionSubmit.setLanguage(language);
        // 设置初始状态
        questionSubmit.setStatus(QuestionSubmitStatusEnum.WAITING.getValue());
        questionSubmit.setJudgeInfo("{}");
        boolean save = this.save(questionSubmit);

        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "数据插入失败");
        }
        // Redis中提交数加1
        String key = QuestionConstant.QUESTION_SUBMIT_NUMBER;
        stringRedisTemplate.opsForZSet().incrementScore(key, String.valueOf(questionId), 1);
        // 设置状态
        String submitStateKey = QuestionSubmitConstant.QUESTION_SUBMIT_STATE_KEY + questionSubmit.getId();
        stringRedisTemplate.opsForValue().set(submitStateKey, QuestionSubmitStatusEnum.WAITING.getValue().toString(), 5, TimeUnit.MINUTES);
        Long questionSubmitId = questionSubmit.getId();

        // 发送消息
        messageProducer.sendMessage("code_exchange", "my_routingKey", String.valueOf(questionSubmitId));

//        HashMap<Object, Object> objectObjectHashMap = new HashMap<>(1);
//        objectObjectHashMap.put("questionSubmitId", questionSubmitId.toString());
//        stringRedisTemplate.opsForStream().add("stream.questionSubmit", objectObjectHashMap);
        // 同步执行判题服务
//        QuestionSubmit questionSubmitResult = judgeService.doJudge(questionSubmitId);

        return questionSubmitId;
    }


    /**
     * 获取查询包装类
     *
     * @param questionSubmitQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<QuestionSubmit> getQueryWrapper(QuestionSubmitQueryRequest questionSubmitQueryRequest) {
        QueryWrapper<QuestionSubmit> queryWrapper = new QueryWrapper<>();
        if (questionSubmitQueryRequest == null) {
            return queryWrapper;
        }

        String language = questionSubmitQueryRequest.getLanguage();
        Integer status = questionSubmitQueryRequest.getStatus();
        Long questionId = questionSubmitQueryRequest.getQuestionId();
        Long userId = questionSubmitQueryRequest.getUserId();
        String sortField = questionSubmitQueryRequest.getSortField();
        String sortOrder = questionSubmitQueryRequest.getSortOrder();

        // 拼接查询条件
        queryWrapper.eq(QuestionSubmitLanguageEnum.getEnumByValue(language) != null, "language", language);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionId), "questionId", questionId);
        queryWrapper.eq(QuestionSubmitStatusEnum.getEnumByValue(status) != null, "status", status);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }


    @Override
    public QuestionSubmitVO getQuestionSubmitVO(QuestionSubmit questionSubmit, User loginUser) {
        QuestionSubmitVO questionSubmitVO = QuestionSubmitVO.objToVo(questionSubmit);
        Long userId = loginUser.getId();
        // 仅本人和管理员能看见自己提交的代码
        if (!userId.equals(questionSubmit.getUserId()) && !userFeignClient.isAdmin(loginUser)) {
            questionSubmitVO.setCode(null);
        }
        return questionSubmitVO;
    }

    @Override
    public Page<QuestionSubmitVO> getQuestionSubmitVOPage(Page<QuestionSubmit> questionSubmitPage, User loginUser) {
        List<QuestionSubmit> questionSubmitList = questionSubmitPage.getRecords();
        Page<QuestionSubmitVO> questionSubmitVOPage = new Page<>(questionSubmitPage.getCurrent(), questionSubmitPage.getSize(), questionSubmitPage.getTotal());
        if (CollectionUtils.isEmpty(questionSubmitList)) {
            return questionSubmitVOPage;
        }
        List<QuestionSubmitVO> questionSubmitVOList = questionSubmitList.stream().map(questionSubmit -> getQuestionSubmitVO(questionSubmit, loginUser)).collect(Collectors.toList());
        questionSubmitVOPage.setRecords(questionSubmitVOList);
        return questionSubmitVOPage;
    }

    @Override
    public Integer getQuestionSubmitState(Long questionSubmitId) {
        String submitStateKey = QuestionSubmitConstant.QUESTION_SUBMIT_STATE_KEY + questionSubmitId;
        String res = stringRedisTemplate.opsForValue().get(submitStateKey);
        if (StringUtils.isBlank(res)) {
            res = "0";
        }
        return Integer.parseInt(res);
    }

    @Override
    public QuestionSubmitStateVO getJudgeInformation(Long questionSubmitId) {
        QuestionSubmit questionSubmit = getById(questionSubmitId);
        QuestionSubmitStateVO questionSubmitStateVO = new QuestionSubmitStateVO();
        questionSubmitStateVO.setStatus(questionSubmit.getStatus());
        questionSubmitStateVO.setJudgeInfo(JSONUtil.parse(questionSubmit.getJudgeInfo()).toBean(JudgeInfo.class));
        return questionSubmitStateVO;
    }

}




