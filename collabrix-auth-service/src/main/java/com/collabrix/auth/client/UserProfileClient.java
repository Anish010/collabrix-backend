package com.collabrix.auth.client;
import com.collabrix.common.libraries.dto.UserProfileCreateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "user-service",
        url = "${services.user-service.url}"
)
public interface UserProfileClient {

    @PostMapping("/api/v1/profile/create")
    void createProfile(@RequestBody UserProfileCreateRequest request);
}
