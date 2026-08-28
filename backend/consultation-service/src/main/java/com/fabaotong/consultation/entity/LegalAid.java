package com.fabaotong.consultation.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("legal_aid")
public class LegalAid {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String applicantName;
    private String idCard;
    private String phone;
    private String address;
    private String caseType;
    private String caseDesc;
    private String evidenceUrls;
    private String incomeStatus;
    private Integer status = 0;
    private Long assignedLawyerId;
    private String reviewRemark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private LocalDateTime reviewTime;
}
