package com.fazhitong.payment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("member")
public class Member {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String memberType;
    private LocalDateTime expireDate;
    private Integer status;
    private LocalDateTime createTime;
}
