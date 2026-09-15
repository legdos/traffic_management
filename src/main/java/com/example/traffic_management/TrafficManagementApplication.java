package com.example.traffic_management;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.traffic_management.mapper") // 扫描 Mapper 接口
public class TrafficManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrafficManagementApplication.class, args);
    }
}