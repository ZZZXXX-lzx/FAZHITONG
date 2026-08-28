package com.fabaotong.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评价评分实体
 */
@Data
@TableName("review")
public class Review {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 评价用户ID */
    private Long userId;

    /** 评价对象类型 */
    private String targetType;

    /** 评价对象ID */
    private Long targetId;

    /** 评分，默认5 */
    private Integer rating = 5;

    /** 评价内容 */
    private String content;

    /** 状态：1-显示，0-隐藏，默认1 */
    private Integer status = 1;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
