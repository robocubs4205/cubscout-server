package com.robocubs4205.cubscout;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.rest.RepositoryRestMvcAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Created by trevor on 2/11/17.
 */
@SpringBootApplication
@EnableTransactionManagement
@EnableAutoConfiguration(exclude = RepositoryRestMvcAutoConfiguration.class)
public class Application extends SpringBootServletInitializer {
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(Application.class);
    }



    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
