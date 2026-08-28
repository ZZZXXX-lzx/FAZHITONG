package com.fazhitong.common.dto;

import lombok.Data;

@Data
public class PageParam {
    private int page = 1;
    private int size = 20;
}
