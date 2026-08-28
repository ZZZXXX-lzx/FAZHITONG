package com.fabaotong.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("lawyer_info")
public class LawyerInfo {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String practiceCertNo;
    private String lawFirm;
    private String specialty;
    private Integer yearsExp;
    private String description;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
