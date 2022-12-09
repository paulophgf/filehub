package br.com.p8projects.filehub.system;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@Configuration
public class SwaggerConfig {

    private static final String description = "The FileHub is a service that standardizes file management, independent " +
            "of the storage platform used. Moreover, it makes file persistence easier when we think about multiple storage " +
            "places, serving as requests gateway, using a safe and easy way.";

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(new ApiInfoBuilder()
                        .description(description)
                        .title("FileHub")
                        .version("1.0.0")
                        .build())
                .enable(true)
                .select()
                .apis(RequestHandlerSelectors.basePackage("br.com.mpps.filehub"))
                .paths(PathSelectors.any())
                .build();
    }

}
