package com.example.project_pulse_backend.controller;

import com.example.project_pulse_backend.dto.request.UpdateUserRequest;
import com.example.project_pulse_backend.dto.response.APIResponse;
import com.example.project_pulse_backend.dto.response.UserResponse;
import com.example.project_pulse_backend.dto.response.UserWithoutCostResponse;
import com.example.project_pulse_backend.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class UserController {
    private UserService userService;

    @GetMapping("/admin")
    public APIResponse<UserResponse> getAllUsers() {
        return APIResponse.<UserResponse>builder()
                .resultList(userService.getAllUsers()).build();
    }

    @GetMapping("/pm")
    public APIResponse<UserWithoutCostResponse> getAllUsersForPM() {
        return APIResponse.<UserWithoutCostResponse>builder()
                .resultList(userService.getAllUsersForPM()).build();
    }

    @GetMapping("/my-info")
    public APIResponse<UserResponse> getMyInfo() {
        return APIResponse.<UserResponse>builder()
                .result(userService.getMyInfo()).build();
    }

    @PutMapping(value = "/update-avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public APIResponse<UserResponse> updateAvatar(MultipartFile avt) {
        return APIResponse.<UserResponse>builder()
                .result(userService.updateAvatar(avt)).build();
    }

    @PutMapping(value = "/admin/update-user", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public APIResponse<UserResponse> updateUser(@RequestBody @Valid UpdateUserRequest request) {
        return APIResponse.<UserResponse>builder()
                .result(userService.updateUser(request)).build();
    }

}
