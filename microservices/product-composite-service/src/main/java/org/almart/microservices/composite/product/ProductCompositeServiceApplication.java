package org.almart.microservices.composite.product;

import org.almart.microservices.composite.product.services.ProductCompositeIntegration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.health.*;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.client.RestTemplate;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebFlux;

import java.util.LinkedHashMap;

import static java.util.Collections.emptyList;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static springfox.documentation.builders.RequestHandlerSelectors.basePackage;
import static springfox.documentation.spi.DocumentationType.SWAGGER_2;

@SpringBootApplication
@ComponentScan("org.almart")
@EnableSwagger2WebFlux
public class ProductCompositeServiceApplication {

	public String apiVersion;
	public String apiTitle;
	public String apiDescription;
	public String apiTermsOfServiceUrl;
	public String apiLicense;
	public String apiLicenseUrl;
	public String apiContactName;
	public String apiContactUrl;
	public String apiContactEmail;

	@Autowired
	public ProductCompositeServiceApplication(
			@Value("${api.common.version}") 		  String apiVersion,
			@Value("${api.common.title}")             String apiTitle,
			@Value("${api.common.description}")       String apiDescription,
			@Value("${api.common.termsOfServiceUrl}") String apiTermsOfServiceUrl,
			@Value("${api.common.license}")           String apiLicense,
			@Value("${api.common.licenseUrl}")        String apiLicenseUrl,
			@Value("${api.common.contact.name}")      String apiContactName,
			@Value("${api.common.contact.url}")       String apiContactUrl,
			@Value("${api.common.contact.email}")     String apiContactEmail
	){
		this.apiVersion = apiVersion;
		this.apiTitle = apiTitle;
		this.apiDescription = apiDescription;
		this.apiTermsOfServiceUrl = apiTermsOfServiceUrl;
		this.apiLicense = apiLicense;
		this.apiLicenseUrl = apiLicenseUrl;
		this.apiContactName = apiContactName;
		this.apiContactUrl = apiContactUrl;
		this.apiContactEmail = apiContactEmail;
	}

	@Bean
	public Docket apiDocumentation() {
		return new Docket(SWAGGER_2)
				.select()
				.apis(basePackage("org.almart.microservices.composite.product"))
				.paths(PathSelectors.any())
				.build()
				.apiInfo(new ApiInfo(
						apiTitle,
						apiDescription,
						apiVersion,
						apiTermsOfServiceUrl,
						new Contact(apiContactName, apiContactUrl,
								apiContactEmail),
						apiLicense,
						apiLicenseUrl,
						emptyList()
				));
	}

	@Autowired
	HealthAggregator healthAggregator;

	@Autowired
	ProductCompositeIntegration integration;

	@Bean
	ReactiveHealthIndicator coreServices() {

		ReactiveHealthIndicatorRegistry registry = new DefaultReactiveHealthIndicatorRegistry(new LinkedHashMap<>());

		registry.register("product", () -> integration.getProductHealth());
		registry.register("recommendation", () -> integration.getRecommendationHealth());
		registry.register("review", () -> integration.getReviewHealth());

		return new CompositeReactiveHealthIndicator(healthAggregator, registry);
	}

	public static void main(String[] args) {
		SpringApplication.run(ProductCompositeServiceApplication.class, args);
	}
}
