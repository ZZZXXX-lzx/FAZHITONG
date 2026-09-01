package com.fazhitong.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 合规体检答题记录
 */
@Data
@TableName("compliance_answer")
public class ComplianceAnswer {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long enterpriseId;
    private Long questionId;
    /** YES / NO / NA */
    private String answer;
    /** 草稿 / 已提交 */
    private Integer status;
    private Long reportId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
