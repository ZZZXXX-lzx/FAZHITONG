package com.fazhitong.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 合规体检报告
 */
@Data
@TableName("compliance_report")
public class ComplianceReport {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long enterpriseId;
    private String reportType;
    private Integer score;
    /** 报告完整 JSON 数据 */
    private String reportData;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
