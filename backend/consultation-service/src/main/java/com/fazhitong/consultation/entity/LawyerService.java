package com.fazhitong.consultation.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("lawyer_service")
public class LawyerService {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long lawyerId;
    private String serviceType;
    private String title;
    private String description;
    private BigDecimal budget;
    private Integer status = 0;
    private String lawyerRemark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private LocalDateTime acceptTime;
    private LocalDateTime finishTime;
}
