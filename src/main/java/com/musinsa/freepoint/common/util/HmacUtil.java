package com.musinsa.freepoint.common.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class HmacUtil {
    public static String generateHmac(String data, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
        mac.init(keySpec);
        byte[] hmacBytes = mac.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(hmacBytes);
    }

    public static boolean validateHmac(String data, String secretKey, String receviedHmac) throws Exception {
        System.out.println("generateHmac(data, secretKey) : "+generateHmac(data, secretKey));
        return generateHmac(data, secretKey).equals(receviedHmac);
    }
}