package com.obsez.zag.common.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import static com.google.common.collect.Sets.*;
import static com.google.common.collect.Sets.newHashSet;

@Configuration
//@Component
@EnableSwagger2
@Import(BeanValidatorPluginsConfiguration.class)
//@ContextConfiguration(initializers = ConfigFileApplicationContextInitializer.class)
@PropertySources({
        @PropertySource(value = {"classpath:/swagger.properties"}, ignoreResourceNotFound = true)
})
//@ImportResource("classpath:apis.xml")
public class SpringFoxConfig extends WebMvcConfigurationSupport {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");

        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    @Bean
    public Docket apiDocket(/*List<SecurityScheme> authorizationTypes*/) {
        //logger.info("new Docket");
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("demo")
                //.genericModelSubstitutes(DeferredResult.class) //
                //.useDefaultResponseMessages(false)
                //.forCodeGeneration(true) //
                .apiInfo(apiInfo()) //
                //.securitySchemes(authorizationTypes)
                //.produces(newHashSet("application/xml", "application/json"))
                .select()
                //.paths(or(
                //        and(
                //                regex("/api/.*"),
                //                not(regex("/api/store/search.*"))),
                //        regex("/generic/.*")))
                .apis(RequestHandlerSelectors.basePackage("com.obsez.zag"))
                .paths(PathSelectors.ant("/**"))
                .build()
                //.host("demo-consumer.swagger.io")
                .protocols(newHashSet("http", "https"));
    }


    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title(title)
                .description(description)
                .termsOfServiceUrl(termsUrl)
                .contact(new Contact(contactName, contactUrl, contactEmail))
                .version(version)
                .license(license)
                .licenseUrl(licenseUrl)
                .build();
        //return new ApiInfo(
        //        "Zag RESTful APIs",
        //        "This application demonstrates documenting os Spring Boot app with Swagger using SpringFox. 更多信息参考WIKI\\uFF1Ahttps\://github.com/hedzr/zag-servers/docs/",
        //        "1.0.0",
        //        "https://github.com/hedzr/zag-servers/terms",
        //        new Contact("hedzr", "https://github.com/hedzr", "hedzrz@gmail.com"),
        //        "MIT License",
        //        "LICENSE URL",
        //        Collections.emptyList()
        //);
    }

    @Value("@swagger.title@")
    String title;
    @Value("${swagger.description}")
    String description;
    @Value("${swagger.termsUrl}")
    String termsUrl;
    @Value("${swagger.contact.name}")
    String contactName;
    @Value("${swagger.contact.url}")
    String contactUrl;
    @Value("${swagger.contact.email}")
    String contactEmail;
    @Value("${swagger.version}")
    String version;
    @Value("${swagger.license}")
    String license;
    @Value("${swagger.licenseUrl}")
    String licenseUrl;

}
