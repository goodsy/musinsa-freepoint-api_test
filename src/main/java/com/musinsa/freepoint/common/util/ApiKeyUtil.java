package com.musinsa.freepoint.common.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class ApiKeyUtil {
    public static String generateApiKey(String id) {
        long ms = System.currentTimeMillis();
        String raw = id + ":" + ms;
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }
}
