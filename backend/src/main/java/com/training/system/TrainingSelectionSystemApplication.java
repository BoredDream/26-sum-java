package com.training.system;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.training.system.**.mapper")
@SpringBootApplication
public class TrainingSelectionSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrainingSelectionSystemApplication.class, args);
    }
}
