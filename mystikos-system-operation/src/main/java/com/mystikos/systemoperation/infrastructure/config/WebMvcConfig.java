package com.mystikos.systemoperation.infrastructure.config;

import com.mystikos.systemoperation.application.service.OperationLogApplicationService;
import com.mystikos.systemoperation.infrastructure.web.OperationLogInterceptor;
import com.mystikos.systemoperation.infrastructure.web.OperationLogRequestWrappingFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** 把操作日志拦截范围收在 {@code /api/v1/manage/**}——跟后台管理接口的鉴权前缀保持一致。 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private static final String MANAGE_PATH_PATTERN = "/api/v1/manage/**";

    private final OperationLogApplicationService operationLogApplicationService;

    public WebMvcConfig(OperationLogApplicationService operationLogApplicationService) {
        this.operationLogApplicationService = operationLogApplicationService;
    }

    @Bean
    public FilterRegistrationBean<OperationLogRequestWrappingFilter> operationLogRequestWrappingFilter() {
        FilterRegistrationBean<OperationLogRequestWrappingFilter> registration = new FilterRegistrationBean<>(
                new OperationLogRequestWrappingFilter());
        registration.addUrlPatterns("/api/v1/manage/*");
        return registration;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new OperationLogInterceptor(operationLogApplicationService))
                .addPathPatterns(MANAGE_PATH_PATTERN);
    }
}
