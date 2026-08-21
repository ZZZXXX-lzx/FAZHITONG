package com.fabaotong.consultation.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("consultation")
public class Consultation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long lawyerId;
    private String title;
    private String question;
    private String answer;
    private String consultationType;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    private LocalDateTime answerTime;
}
