package com.fazhitong.contract.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 合同审查风险点
 */
@Data
@TableName("contract_review_risk")
public class ContractReviewRisk {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    /** HIGH / MEDIUM / LOW */
    private String level;
    /** 条款原文 */
    private String clause;
    /** 风险说明 */
    private String description;
    /** 修改建议 */
    private String suggestion;
    /** 法律依据 */
    private String legalBasis;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
