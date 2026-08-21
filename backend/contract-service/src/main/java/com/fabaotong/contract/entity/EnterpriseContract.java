package com.fabaotong.contract.entity;

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
    private String status;
    private LocalDateTime signDate;
    private LocalDateTime expireDate;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
