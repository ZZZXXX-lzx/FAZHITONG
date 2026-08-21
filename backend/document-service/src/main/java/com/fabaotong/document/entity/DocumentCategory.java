package com.fabaotong.document.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("document_category")
public class DocumentCategory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String code;
    private Long parentId;
    private Integer sort;
    private Integer status;
}
