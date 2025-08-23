package co.com.bancodebogota.bdbapprovals.infrastructure.config;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI api() {
        return new OpenAPI().info(new Info()
                        .title("BDB Approvals API")
                        .version("v1")
                        .description("Flujo genérico de aprobación - Banco de Bogotá"))
                .externalDocs(new ExternalDocumentation().description("Postman/OpenAPI").url("about:blank"));
    }
}
