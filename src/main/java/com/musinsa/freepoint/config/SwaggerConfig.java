
package com.musinsa.freepoint.config;

import com.musinsa.freepoint.adapters.in.web.ApiHeaderConstants;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springdoc.core.customizers.OpenApiCustomizer;


@Configuration
public class SwaggerConfig {
   /* @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info().title("Musinsa Free Point API").version("v1"))
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
                .components(new Components()
                        .addParameters(ApiHeaderConstants.HEADER_MUSINSA_ID,
                                new Parameter().in("header").name(ApiHeaderConstants.HEADER_MUSINSA_ID).required(true).description("Musinsa ID"))
                        .addParameters(ApiHeaderConstants.IDEMPOTENCY_KEY,
                                new Parameter().in("header").name(ApiHeaderConstants.IDEMPOTENCY_KEY).required(false).description("Idempotency Key (optional)"))
                        .addSecuritySchemes("BearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .in(SecurityScheme.In.HEADER)
                                        .name("Authorization")
                        )
                );

    }*/
   private static final String BEARER = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info().title("Musinsa Free Point API").version("v1"))
                .components(new Components()
                        .addSecuritySchemes(BEARER,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        )
                );
    }

    /**
     * 전역 헤더 주입 (간단판)
     * - GET: Musinsa-Id만
     * - POST/PUT/PATCH/DELETE: Musinsa-Id + Idempotency-Key
     * - 중복 체크/복잡한 로직은 생략해 최소화
     */
    @Bean
    public OpenApiCustomizer addCommonHeaders() {
        return openApi -> openApi.getPaths().values().forEach(pi -> {
            // 공통 헤더 정의
            Parameter musinsaId = new Parameter()
                    .in("header").name(ApiHeaderConstants.HEADER_MUSINSA_ID)
                    .required(true).description("Musinsa ID");
            Parameter idemKey = new Parameter()
                    .in("header").name(ApiHeaderConstants.IDEMPOTENCY_KEY)
                    .required(false).description("Idempotency Key");

            if (pi.getGet() != null)    pi.getGet().addParametersItem(musinsaId);
            if (pi.getPost() != null)  { pi.getPost().addParametersItem(musinsaId);  pi.getPost().addParametersItem(idemKey); }
            if (pi.getPut() != null)   { pi.getPut().addParametersItem(musinsaId);   pi.getPut().addParametersItem(idemKey); }
            if (pi.getPatch() != null) { pi.getPatch().addParametersItem(musinsaId); pi.getPatch().addParametersItem(idemKey); }
            if (pi.getDelete() != null){ pi.getDelete().addParametersItem(musinsaId);pi.getDelete().addParametersItem(idemKey); }
        });
    }
}
