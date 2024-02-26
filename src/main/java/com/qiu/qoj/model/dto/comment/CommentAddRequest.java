package com.qiu.qoj.model.dto.comment;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 创建请求
 *
 * 
 * 
 */
@Data
public class CommentAddRequest implements Serializable {

    /**
     * 内容
     */
    private String content;

    /**
     * 题解 id
     */
    private Long questionSolvingId;

    /**
     * 父评论 id
     */
    private Long fatherCommentId;



    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}