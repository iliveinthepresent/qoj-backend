package com.qiu.qojbackenduserservice.controller.inner;

import com.qiu.qojbackendmodel.entity.User;
import com.qiu.qojbackendserviceclient.service.UserFeignClient;
import com.qiu.qojbackenduserservice.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

/**
 * 用户接口
 */
@RestController
@RequestMapping("/inner")
@Slf4j
public class UserInnerController implements UserFeignClient {

    @Resource
    private UserService userService;




    @Override
    @GetMapping("/get/id")
    public User getById(@RequestParam("userId") long userId) {
        return userService.getById(userId);
    }

    @Override
    @GetMapping("get/ids")
    public List<User> listByIds(@RequestBody Collection<Long> idList) {
        return userService.listByIds(idList);
    }

}
