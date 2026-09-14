package com.example.project_pulse_backend.repository.httpClient;

import com.example.project_pulse_backend.dto.response.OutboundUserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "outbound-user-client", url = "https://www.googleapis.com")
public interface OutboundUserClient {

    @GetMapping("/oauth2/v2/userinfo")
    OutboundUserResponse getUserDetails(@RequestHeader("Authorization") String authorization);
}
