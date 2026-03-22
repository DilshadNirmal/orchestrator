package com.orchestrator.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
         return new OpenAPI()
                .info(new Info()
                        .title("DataSync Orchestrator API")
                        .version("1.0.0")
                        .description("A flexible job orchestrator for syncing MongoDB data to PostgreSQL")
                        .contact(new Contact()
                                .name("Dilshad Nirmal")
                                .email("dilshadgov2020@gmail.com")));
    }
}
