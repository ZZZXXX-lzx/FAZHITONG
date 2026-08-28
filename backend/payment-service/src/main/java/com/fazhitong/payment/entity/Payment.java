package com.fazhitong.payment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("payment")
public class Payment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private String paymentMethod;
    private BigDecimal amount;
    private String transactionNo;
    private Integer status;
    private LocalDateTime createTime;
}
