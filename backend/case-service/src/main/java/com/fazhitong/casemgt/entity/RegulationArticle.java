package com.fazhitong.casemgt.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("regulation_article")
public class RegulationArticle {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long regulationId;
    @TableField("article_no")
    private String articleNo;
    private String content;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}