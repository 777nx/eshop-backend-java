package com.eshop;

import com.eshop.utils.SpringContextHolder;
import com.binarywang.spring.starter.wxjava.miniapp.config.WxMaAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author zhonghui
 * @date 2019/10/1 9:20:19
 */
@EnableAsync
@EnableTransactionManagement
@EnableCaching
@MapperScan(basePackages ={"com.eshop.modules.*.service.mapper", "com.eshop.config"})
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class , WxMaAutoConfiguration.class})
public class ApiRun {

    public static void main(String[] args) {
        SpringApplication.run(ApiRun.class, args);

        System.out.println();
    }

    @Bean
    public SpringContextHolder springContextHolder() {
        return new SpringContextHolder();
    }
}
