package com.fazhitong.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 合规体检题目
 */
@Data
@TableName("compliance_question")
public class ComplianceQuestion {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 领域：LABOR/CONTRACT/IP/DATA/TAX */
    private String domain;
    /** 题目内容 */
    private String content;
    /** 权重 0-10 */
    private Integer weight;
    /** 排序 */
    private Integer sort;
    /** 状态 1 启用 */
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
