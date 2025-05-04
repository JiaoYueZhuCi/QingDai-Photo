package com.qingdai;

import org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Import;

@SpringBootApplication(scanBasePackages = "com.qingdai")
@MapperScan("com.qingdai.mapper")
@EnableCaching
@Import(RocketMQAutoConfiguration.class)
public class QingDaiSpApplication {

    public static void main(String[] args) {
        SpringApplication.run(QingDaiSpApplication.class, args);
    }

}
