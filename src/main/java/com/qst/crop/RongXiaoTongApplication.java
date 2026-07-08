package com.qst.crop;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.qst.crop.dao")
public class RongXiaoTongApplication {
    public static void main(String[] args) {
        SpringApplication.run(RongXiaoTongApplication.class, args);
    }
}