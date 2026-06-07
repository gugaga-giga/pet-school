package com.petschool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan({"com.petschool.mapper", "com.petschool.ai.mapper"})
public class PetSchoolApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(PetSchoolApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(PetSchoolApplication.class, args);
    }
}
