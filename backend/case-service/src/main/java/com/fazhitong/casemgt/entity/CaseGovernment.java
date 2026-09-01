package com.fazhitong.casemgt.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("case_government")
public class CaseGovernment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String causeName;
    private String courtName;
    private String caseYear;
    private String judgmentResult;
    private String keywords;
    @TableField("`abstract`")
    private String abstractText;
    private String focusPoints;
    private String fullText;
    private String source;
    private Integer status;
    /** 判决依据（智能提炼缓存） */
    private String judgmentBasis;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(exist = false)
    private Double score;
}

