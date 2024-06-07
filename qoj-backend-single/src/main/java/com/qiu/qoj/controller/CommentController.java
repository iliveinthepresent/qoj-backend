package com.qiu.qoj.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiu.qoj.common.BaseResponse;
import com.qiu.qoj.common.DeleteRequest;
import com.qiu.qoj.common.ErrorCode;
import com.qiu.qoj.common.ResultUtils;
import com.qiu.qoj.exception.BusinessException;
import com.qiu.qoj.exception.ThrowUtils;
import com.qiu.qoj.model.dto.comment.CommentAddRequest;
import com.qiu.qoj.model.dto.comment.CommentQueryRequest;
import com.qiu.qoj.model.entity.Comment;
import com.qiu.qoj.model.entity.User;
import com.qiu.qoj.model.vo.CommentVO;
import com.qiu.qoj.service.CommentService;
import com.qiu.qoj.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 题目接口
 */
@RestController
@RequestMapping("/comment")
@Slf4j
public class CommentController {

    @Resource
    private CommentService commentService;

    @Resource
    private UserService userService;


    // region 增删改查

    /**
     * 创建
     *
     * @param commentAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addComment(@RequestBody CommentAddRequest commentAddRequest, HttpServletRequest request) {
        if (commentAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Comment comment = new Comment();
        BeanUtils.copyProperties(commentAddRequest, comment);

        // todo 无效参数校验

        User loginUser = userService.getLoginUser(request);
        comment.setUserId(loginUser.getId());


        boolean result = commentService.save(comment);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newCommentId = comment.getId();
        return ResultUtils.success(newCommentId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteComment(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Comment oldComment = commentService.getById(id);
        ThrowUtils.throwIf(oldComment == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldComment.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = commentService.removeById(id);
        return ResultUtils.success(b);
    }


    /**
     * 分页获取题解列表
     *
     * @param commentQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<CommentVO>> listCommentPageVOByPage(@RequestBody CommentQueryRequest commentQueryRequest,
                                                                 HttpServletRequest request) {
        long current = commentQueryRequest.getCurrent();
        long size = commentQueryRequest.getPageSize();
        Page<Comment> commentPage = commentService.page(new Page<>(current, size), commentService.getQueryWrapper(commentQueryRequest)
        );
        Page<CommentVO> commentPageVO = commentService.getCommentPageVO(commentPage, request);
        return ResultUtils.success(commentPageVO);
    }

    /**
     * 给评论点赞
     * @param id
     * @param httpServletRequest
     * @return
     */
    @PutMapping("/like/{id}")
    public BaseResponse likeComment(@PathVariable("id") Long id, HttpServletRequest httpServletRequest) {
        Boolean success = commentService.likeComment(id, httpServletRequest);
        return ResultUtils.success(null);
    }


}
