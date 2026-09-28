package com.office.purchase;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 办公用品采购管理系统启动类。
 * MapperScan 让 MyBatis-Plus 识别持久层接口。
 */
@SpringBootApplication
@MapperScan("com.office.purchase.mapper")
public class PurchaseApplication {

    /**
     * 启动内置 Tomcat，并加载数据源与业务组件。
     */
    public static void main(String[] args) {
        SpringApplication.run(PurchaseApplication.class, args);
    }
}
