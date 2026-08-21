package com.fabaotong.consultation;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.fabaotong")
@MapperScan("com.fabaotong.consultation.mapper")
public class ConsultationApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConsultationApplication.class, args);
    }
}
