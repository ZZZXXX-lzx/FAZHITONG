package com.fazhitong.consultation.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 律师承办案件台账
 */
@Data
@TableName("lawyer_case")
public class LawyerCase {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 承办律师用户 ID */
    private Long lawyerId;
    /** 案件名称 */
    private String caseName;
    /** 当事人名称 */
    private String clientName;
    /** 案由/案件类型 */
    private String caseType;
    /** 状态：IN_PROGRESS 承办中 / CLOSED 已结案 / ARCHIVED 已归档 */
    private String status;
    /** 案情描述 */
    private String description;
    /** 备注 */
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    /** 结案时间 */
    private LocalDateTime closeTime;
}
