package com.qst.crop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
// 扫描mapper接口所在包，和yml中mapper路径对应

public class RongXiaoTongApplication {
    public static void main(String[] args) {
        SpringApplication.run(RongXiaoTongApplication.class, args);
    }
}