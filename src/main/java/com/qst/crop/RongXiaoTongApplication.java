package com.qst.crop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class RongXiaoTongApplication {
    public static void main(String[] args) {
        SpringApplication.run(RongXiaoTongApplication.class, args);
    }
}