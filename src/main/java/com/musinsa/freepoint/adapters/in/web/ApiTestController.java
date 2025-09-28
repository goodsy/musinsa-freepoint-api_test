
package com.musinsa.freepoint.adapters.in.web;

import com.musinsa.freepoint.common.util.ApiKeyUtil;
import com.musinsa.freepoint.common.util.HmacUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
import java.util.UUID;


@RestController
@RequestMapping("/api/v1/demo")
public class ApiTestController {
    @GetMapping("/health") public Map<String,Object> health(){
        return Map.of("status","OK");
    }

    @GetMapping("/apikey")
    public Map<String, Object> generateApiKey(@RequestParam String id) {
        String apiKey = ApiKeyUtil.generateApiKey(id);
        return Map.of("apiKey", apiKey);
    }

    @GetMapping("/bearer")
    public Map<String, Object> generateHmac(@RequestParam String method,
                                            @RequestParam String uri,
                                            @RequestParam String apikey
    ) throws Exception {
        String requestData = method + uri;
        String hmac = HmacUtil.generateHmac(requestData, apikey);
        return Map.of("Bearer", hmac);
    }


    @GetMapping("/idempotencyKey")
    public Map<String, Object> generateHmac() throws Exception {
        String idempotencyKey = UUID.randomUUID().toString().replace("-", "")+ "-" + System.currentTimeMillis();
        return Map.of("idempotencyKey", idempotencyKey);
    }
}
