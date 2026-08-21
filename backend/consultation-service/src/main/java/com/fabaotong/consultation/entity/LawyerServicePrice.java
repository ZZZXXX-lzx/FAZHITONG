package com.fabaotong.consultation.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("lawyer_service_price")
public class LawyerServicePrice {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long lawyerId;
    private String serviceType;
    private String serviceName;
    private BigDecimal price;
    private String unit;
    private String description;
    private Integer status = 1;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
