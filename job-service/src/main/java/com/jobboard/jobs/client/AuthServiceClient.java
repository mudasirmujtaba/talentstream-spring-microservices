package com.jobboard.jobs.client;

import com.jobboard.jobs.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "auth-service")  // Service name in Eureka
public interface AuthServiceClient {

    @GetMapping("/api/auth/me")
    UserDTO getCurrentUser(@RequestHeader("Authorization") String token);

    // ADD THIS METHOD - Get users subscribed to a category
    @GetMapping("/api/auth/preferences/subscribed-users/{category}")
    List<UserDTO> getUsersSubscribedToCategory(@PathVariable("category") String category);
}