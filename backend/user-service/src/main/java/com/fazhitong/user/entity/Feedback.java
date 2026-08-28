package com.fazhitong.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 意见反馈实体
 */
@Data
@TableName("feedback")
public class Feedback {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 反馈类型，默认SUGGESTION */
    private String type = "SUGGESTION";

    /** 反馈标题 */
    private String title;

    /** 反馈内容 */
    private String content;

    /** 联系方式 */
    private String contact;

    /** 状态：0-待处理，1-已回复，默认0 */
    private Integer status = 0;

    /** 回复内容 */
    private String reply;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 回复时间 */
    private LocalDateTime replyTime;
}
