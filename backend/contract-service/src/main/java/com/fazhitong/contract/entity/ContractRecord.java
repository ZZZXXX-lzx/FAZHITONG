package com.fazhitong.contract.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("contract_record")
public class ContractRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long enterpriseId;
    private String title;
    private String fileUrl;
    private String riskReport;
    private String riskLevel;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    private LocalDateTime reviewTime;
}
