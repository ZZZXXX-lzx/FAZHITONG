package com.fazhitong.contract.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 知识产权台账
 */
@Data
@TableName("ip_record")
public class IpRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long enterpriseId;
    /** 类型：TRADEMARK 商标 / PATENT 专利 / COPYRIGHT 著作权 */
    private String ipType;
    /** 名称 */
    private String name;
    /** 注册号 / 申请号 / 登记号 */
    private String registerNo;
    /** 权利状态：PENDING 申请中 / GRANTED 已授权 / INVALID 已失效 */
    private String status;
    /** 申请日期 */
    private LocalDateTime applyDate;
    /** 到期日期 */
    private LocalDateTime expireDate;
    /** 权利人 */
    private String owner;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    /** 距离到期剩余天数（非数据库字段） */
    @TableField(exist = false)
    private Integer daysToExpire;
}
