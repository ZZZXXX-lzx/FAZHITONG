package com.fazhitong.contract.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 企业投融资台账
 */
@Data
@TableName("investment_record")
public class InvestmentRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long enterpriseId;
    /** 类型：FINANCING 融资 / INVESTMENT 对外投资 */
    private String type;
    /** 轮次：天使轮 / Pre-A / A轮 / B轮 / C轮 / D轮 / 战略投资 / 其他 */
    private String round;
    /** 投资方 / 被投企业 */
    private String investor;
    /** 金额（万元） */
    private BigDecimal amount;
    /** 投后估值（万元） */
    private BigDecimal valuation;
    /** 股权比例（%） */
    private BigDecimal equityRatio;
    /** 时间 */
    private LocalDateTime investDate;
    /** 状态：PLANNED 计划中 / IN_PROGRESS 进行中 / COMPLETED 已完成 / TERMINATED 已终止 */
    private String status;
    /** 备注 */
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
