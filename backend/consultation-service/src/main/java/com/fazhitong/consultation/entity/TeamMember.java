package com.fazhitong.consultation.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 律所团队成员
 */
@Data
@TableName("team_member")
public class TeamMember {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 所属律师 / 团队负责人用户 ID */
    private Long lawyerId;
    /** 成员姓名 */
    private String name;
    /** 角色：PARTNER 合伙人 / LAWYER 执业律师 / ASSISTANT 律师助理 / INTERN 实习生 / ADMIN 行政 */
    private String role;
    /** 联系电话 */
    private String phone;
    /** 邮箱 */
    private String email;
    /** 状态：ACTIVE 在职 / INACTIVE 离职 */
    private String status;
    /** 入职日期 */
    private LocalDate joinDate;
    /** 备注 */
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
