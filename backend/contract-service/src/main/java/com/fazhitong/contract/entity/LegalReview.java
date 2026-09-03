package com.fazhitong.contract.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 企业法律审核台账
 */
@Data
@TableName("legal_review")
public class LegalReview {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long enterpriseId;
    /** 审核事项标题 */
    private String title;
    /** 审核类型：CONTRACT 合同审核 / DOCUMENT 文件审核 / COMPLIANCE 合规审核 / OTHER 其他 */
    private String reviewType;
    /** 审核内容摘要 */
    private String content;
    /** 提交人 / 部门 */
    private String submitter;
    /** 状态：PENDING 待审核 / REVIEWING 审核中 / APPROVED 已通过 / REJECTED 已驳回 */
    private String status;
    /** 审核人 */
    private String reviewer;
    /** 审核意见 */
    private String opinion;
    /** 审核时间 */
    private LocalDateTime reviewTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
