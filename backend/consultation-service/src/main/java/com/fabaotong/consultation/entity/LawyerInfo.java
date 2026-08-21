package com.fabaotong.consultation.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * lawyer_info 表的本地只读映射（用于律师大厅查询）。
 * 与 user-service 共享同一数据库，此处仅做查询用途。
 */
@Data
@TableName("lawyer_info")
public class LawyerInfo {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String practiceCertNo;
    private String lawFirm;
    private String specialty;
    private Integer yearsExp;
    private String description;
    private Integer status;
}
