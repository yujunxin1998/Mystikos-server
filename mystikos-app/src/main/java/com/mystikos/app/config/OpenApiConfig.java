package com.mystikos.app.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_AUTH = "bearerAuth";

    /**
     * 全局声明认证方案，Knife4j 界面右上角会出现"Authorize"按钮。
     *
     * <p>用 apiKey 类型而不是 HTTP bearer 类型——踩过坑：Knife4j 4.3.0 的调试面板
     * 对 HTTP bearer 类型的映射不可靠，会把安全方案的 name（"bearerAuth"）直接当成
     * 请求头字段名发出去，而不是标准的 {@code Authorization: Bearer <token>}，
     * 导致 JwtAuthenticationFilter 收不到 token、请求被当成匿名访问。
     * apiKey 类型明确指定了头名叫 Authorization，Knife4j 对这种类型的映射是稳的。
     *
     * <p>因为是 apiKey，不会自动加 "Bearer " 前缀——在 Authorize 弹窗里要填完整的
     * "Bearer eyJxxx..."（含前缀和空格），不是只填 token 本身。
     *
     * <p>全局声明会让不需要登录的接口（注册/登录本身）在文档里也显示一个锁图标，
     * 这只是文档展示上的小瑕疵，不影响这些接口实际不校验 token。
     */
    @Bean
    public OpenAPI mystikosOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Mystikos Server API")
                        .description("陪玩公会平台后端接口文档（Knife4j / OpenAPI 3）")
                        .version("1.0-SNAPSHOT"))
                .components(new Components().addSecuritySchemes(BEARER_AUTH,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("Authorization")
                                .description("登录/注册接口返回的 accessToken，这里要填完整的 \"Bearer <token>\""
                                        + "（含 Bearer 前缀和空格）——apiKey 类型不会自动加前缀")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
    }
}
