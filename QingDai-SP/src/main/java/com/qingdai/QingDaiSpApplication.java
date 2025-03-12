package com.qingdai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.qingdai")
@MapperScan("com.qingdai.mapper")
public class QingDaiSpApplication {

    public static void main(String[] args) {
        SpringApplication.run(QingDaiSpApplication.class, args);
    }

}
