package ru.svifty7.services.domain.rudagames.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.svifty7.services.domain.rudagames.config.AuthPayload;
import ru.svifty7.services.domain.rudagames.dto.AuthResponse;

@FeignClient(name = "auth-feign", url = "${rudagames.api.baseUrl}")
public interface AuthFeign {

    @PostMapping("${rudagames.api.signIn}")
    AuthResponse signIn(@RequestBody AuthPayload authPayload);

}
