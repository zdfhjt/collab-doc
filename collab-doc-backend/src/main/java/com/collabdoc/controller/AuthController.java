package com.collabdoc.controller;

import com.collabdoc.dto.auth.AuthResponse;
import com.collabdoc.dto.auth.ChangePasswordRequest;
import com.collabdoc.dto.auth.LoginRequest;
import com.collabdoc.dto.auth.RegisterRequest;
import com.collabdoc.dto.auth.UpdateProfileRequest;
import com.collabdoc.dto.common.ApiResponse;
import com.collabdoc.service.AuthService;
import com.collabdoc.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Registration successful"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Login successful"));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<Void>> updateProfile(@RequestBody UpdateProfileRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        authService.updateProfile(userId, request.getDisplayName());
        return ResponseEntity.ok(ApiResponse.ok(null, "Profile updated"));
    }

    @PostMapping("/avatar")
    public ResponseEntity<ApiResponse<String>> uploadAvatar(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        Long userId = SecurityUtil.getCurrentUserId();
        String avatarUrl = authService.uploadAvatar(userId, file);
        return ResponseEntity.ok(ApiResponse.ok(avatarUrl, "Avatar uploaded"));
    }

    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        authService.changePassword(userId, request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.ok(null, "Password changed"));
    }
}
