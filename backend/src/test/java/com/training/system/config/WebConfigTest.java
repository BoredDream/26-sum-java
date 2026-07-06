package com.training.system.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class WebConfigTest {
    @Test
    @DisplayName("CORS 配置允许 PATCH，支持审核/反馈类接口")
    void addCorsMappings_allowsPatch() {
        TestCorsRegistry registry = new TestCorsRegistry();
        WebConfig config = new WebConfig(mock(LoginInterceptor.class));

        config.addCorsMappings(registry);

        CorsConfiguration cors = registry.configurations().get("/**");
        assertNotNull(cors);
        assertTrue(cors.getAllowedMethods().contains("PATCH"));
        assertTrue(cors.getAllowedMethods().contains("OPTIONS"));
        assertEquals(Boolean.TRUE, cors.getAllowCredentials());
    }

    private static class TestCorsRegistry extends CorsRegistry {
        Map<String, CorsConfiguration> configurations() {
            return getCorsConfigurations();
        }
    }
}
