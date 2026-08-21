package com.fabaotong.document.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("document_record")
public class DocumentRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long templateId;
    @TableField("`data`")
    private String data;
    private String fileUrl;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
