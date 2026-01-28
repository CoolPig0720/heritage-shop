package com.heritage.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("非遗产品智能定制商城系统 API")
                .version("1.0.0")
                .description("基于 Spring Boot 的非遗产品智能定制商城系统接口文档")
                .contact(new Contact()
                    .name("Heritage Shop Team")
                    .email("support@heritage.com"))
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}
