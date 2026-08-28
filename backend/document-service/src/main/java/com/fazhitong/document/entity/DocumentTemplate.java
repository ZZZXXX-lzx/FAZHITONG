package com.fazhitong.document.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("document_template")
public class DocumentTemplate {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String type;
    private String name;
    @TableField("`content`")
    private String content;
    private String category;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
