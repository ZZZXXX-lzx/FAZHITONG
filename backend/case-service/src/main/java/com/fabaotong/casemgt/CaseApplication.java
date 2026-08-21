package com.fabaotong.casemgt;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.fabaotong")
@MapperScan("com.fabaotong.casemgt.mapper")
public class CaseApplication {
    public static void main(String[] args) {
        SpringApplication.run(CaseApplication.class, args);
    }
}

