package com.homeinventory;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
    info = @Info(
        title = "物品位置管理系统 API",
        version = "1.0.0",
        description = "记录和管理家中物品位置的系统"
    )
)
public class HomeInventoryApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(HomeInventoryApplication.class, args);
    }
}
