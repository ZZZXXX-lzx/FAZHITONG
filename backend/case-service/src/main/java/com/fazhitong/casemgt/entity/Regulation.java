package com.fazhitong.casemgt.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

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
    @TableField(exist = false)
    private List<RegulationArticle> articles;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
