package com.productmanagement.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Product Management API")
                        .version("v1")
                        .description("Documentação da API de gerenciamento de produtos")
                        .contact(new Contact()
                                .name("Kauan Dalla Corte")
                                .email("kauanjdallacorte@gmail.com")
                                .url("https://github.com/kauan-dalla-corte"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                new Server().url("http://localhost:8080").description("Local")
        ));
    }
}