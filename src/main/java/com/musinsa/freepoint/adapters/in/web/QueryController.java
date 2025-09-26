
package com.musinsa.freepoint.adapters.in.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
@RestController @RequestMapping("/api/v1/points")
public class QueryController {
    @GetMapping("/health") public Map<String,Object> health(){ return Map.of("status","OK"); }
}
