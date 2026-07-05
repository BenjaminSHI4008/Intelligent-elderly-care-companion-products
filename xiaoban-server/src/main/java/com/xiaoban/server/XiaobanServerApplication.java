package com.xiaoban.server;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.xiaoban.server.mapper")
public class XiaobanServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(XiaobanServerApplication.class, args);
    }
}
