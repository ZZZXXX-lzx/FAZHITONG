package com.fazhitong.casemgt.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("regulation")
public class Regulation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String lawType;
    private String issuingAuthority;
    private String publishDate;
    private String effectiveDate;
    private String status;
    private String content;
    private String keywords;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
