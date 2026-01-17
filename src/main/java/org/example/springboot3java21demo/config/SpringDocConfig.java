package org.example.springboot3java21demo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringDocConfig {
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .packagesToScan("org.example.springboot3java21demo")
                .build();
    }

    @Bean
    public OpenAPI apiInfo() {
        return new OpenAPI().info(
                new Info()
                        .title("接口文档，示例")
                        .description("测试接口文档 ")
                        .version("1.0.0")
                        .license(new License().name("阿帕奇 2.0").url("http://springdoc.org"))
        );
    }
}

