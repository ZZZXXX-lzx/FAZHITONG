package com.fazhitong.casemgt.dto;

import lombok.Data;

/**
 * 单行法规明细，用于文件批量导入。同一 法规名称(法规名称) 的行会合并到一部法规下。
 */
@Data
public class RegulationImportRow {
    private String title;            // 法规名称
    private String lawType;          // 法规类型
    private String issuingAuthority; // 制定机关
    private String publishDate;      // 发布日期
    private String effectiveDate;    // 施行日期
    private String status;           // 效力状态
    private String keywords;         // 关键词
    private String overview;         // 法规简介/全文概要
    private String articleNo;        // 条文序号
    private String articleContent;   // 条文内容
}