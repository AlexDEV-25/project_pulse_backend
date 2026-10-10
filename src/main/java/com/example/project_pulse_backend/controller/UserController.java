package com.example.project_pulse_backend.controller;

import com.example.project_pulse_backend.dto.request.UpdateUserRequest;
import com.example.project_pulse_backend.dto.response.APIResponse;
import com.example.project_pulse_backend.dto.response.UserResponse;
import com.example.project_pulse_backend.dto.response.UserStatisticsResponse;
import com.example.project_pulse_backend.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class UserController {
    private UserService userService;

    @GetMapping
    public APIResponse<UserResponse> getAllUsers(
            @RequestParam(required = false) List<String> roleNames,
            @RequestParam(required = false) List<Long> departmentIds,
            @RequestParam(required = false) String keyword) {
        return APIResponse.<UserResponse>builder()
                .resultList(userService.getUsers(roleNames, departmentIds, keyword)).build();
    }

    @GetMapping("/statistics")
    public APIResponse<UserStatisticsResponse> getUserStatistics() {
        return APIResponse.<UserStatisticsResponse>builder()
                .result(userService.getUserStatistics()).build();
    }

    @GetMapping("/{id}")
    public APIResponse<UserResponse> getById(@PathVariable Long id) {
        return APIResponse.<UserResponse>builder()
                .result(userService.getById(id)).build();
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

    @PutMapping("/admin/update-user")
    public APIResponse<UserResponse> updateUser(@RequestBody @Valid UpdateUserRequest request) {
        return APIResponse.<UserResponse>builder()
                .result(userService.updateUser(request)).build();
    }

}
