package com.fabaotong.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息通知实体
 */
@Data
@TableName("notification")
public class Notification {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 接收用户ID */
    private Long userId;

    /** 通知标题 */
    private String title;

    /** 通知内容 */
    private String content;

    /** 通知类型，默认SYSTEM */
    private String type = "SYSTEM";

    /** 关联业务ID */
    private Long refId;

    /** 是否已读：0-未读，1-已读，默认0 */
    private Integer isRead = 0;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
