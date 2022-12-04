package br.com.mpps.filehub.system;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    private static final String description = "The FileHub is a service that standardizes file management, independent " +
            "of the storage platform used. Moreover, it makes file persistence easier when we think about multiple storage " +
            "places, serving as requests gateway, using a safe and easy way.";

    @Bean
    public Docket apiPlanModule() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("FileHub")
                .select()
                .apis(RequestHandlerSelectors.basePackage("br.com.mpps.filehub"))
                .paths(PathSelectors.any())
                .build()
                .apiInfo(new ApiInfoBuilder()
                        .title("FileHub API")
                        .description(description)
                        .version("Version 1")
                        .build()).ignoredParameterTypes(ResponseEntity.class)
                .produces(Collections.singleton("application/json"))
                .useDefaultResponseMessages(false);
    }

}
