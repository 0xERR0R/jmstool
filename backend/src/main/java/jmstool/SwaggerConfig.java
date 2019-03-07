package jmstool;

import static springfox.documentation.builders.PathSelectors.regex;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
/**
 * Swagger configuration for REST API documentation
 * 
 */
public class SwaggerConfig {
	@Bean
	public Docket productApi() {
		return new Docket(DocumentationType.SWAGGER_2) //
				.useDefaultResponseMessages(false) //
				.select() //
				.apis(RequestHandlerSelectors.basePackage("jmstool.controller")) //
				.paths(regex("/.*")).build() //
				.apiInfo(metaData());

	}

	private ApiInfo metaData() {
		return new ApiInfoBuilder() //
				.title("JmsTool REST API") //
				.build();
	}

}
