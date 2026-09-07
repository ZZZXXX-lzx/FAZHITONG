package com.fazhitong.casemgt.dto;

import lombok.Data;

@Data
public class ImportResult {
    private int createdRegulations;   // 本次新建的法规数
    private int appendedRegulations;  // 追加条文的已有法规数
    private int provisions;           // 导入的条文总数
    private int skipped;              // 跳过(内容为空)的条文数
}