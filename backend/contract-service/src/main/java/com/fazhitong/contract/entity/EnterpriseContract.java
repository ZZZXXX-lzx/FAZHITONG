package com.fazhitong.contract.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("enterprise_contract")
public class EnterpriseContract {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long enterpriseId;
    private String title;
    private String contractNo;
    private String fileUrl;
    private String partyA;
    private String partyB;
    private String amount;
    /** DRAFT 草稿 / PENDING_SIGN 待签署 / SIGNED 已签署 / ARCHIVED 已归档 / EXPIRED 已过期 */
    private String status;
    private LocalDateTime signDate;
    private LocalDateTime expireDate;
    /** 签署人 */
    private String signerName;
    /** 电子签章凭据号 */
    private String signCertNo;
    /** 签署时间 */
    private LocalDateTime signTime;
    /** 归档时间 */
    private LocalDateTime archiveTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    /** 距离到期剩余天数（非数据库字段，用于到期预警展示） */
    @TableField(exist = false)
    private Integer daysToExpire;
}
