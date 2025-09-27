package com.musinsa.freepoint.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

class ApiKeyGeneratorTest {

    @Test
    @DisplayName("API_KEY 생성 테스트")
    void generateApiKey() throws Exception {
        String secret = "musinsaId:"+System.currentTimeMillis();

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(secret.getBytes(StandardCharsets.UTF_8));

        String apiKey = Base64.getEncoder().encodeToString(hash);

        System.out.println("Generated API_KEY: " + apiKey);
    }
}
