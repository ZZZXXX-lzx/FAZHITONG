package com.fazhitong.consultation.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 律师客户信息
 */
@Data
@TableName("lawyer_client")
public class LawyerClient {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 所属律师用户 ID */
    private Long lawyerId;
    /** 客户名称 */
    private String clientName;
    /** 联系电话 */
    private String phone;
    /** 类型：PERSONAL 个人 / ENTERPRISE 企业 */
    private String clientType;
    /** 备注 */
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
