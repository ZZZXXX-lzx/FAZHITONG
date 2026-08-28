package com.fazhitong.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("enterprise_info")
public class EnterpriseInfo {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String enterpriseName;
    private String creditCode;
    private String enterpriseScale;
    private String contactName;
    private String contactPhone;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
