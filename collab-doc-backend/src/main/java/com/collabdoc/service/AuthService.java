package com.collabdoc.service;

import com.collabdoc.dto.auth.AuthResponse;
import com.collabdoc.dto.auth.LoginRequest;
import com.collabdoc.dto.auth.RegisterRequest;
import org.springframework.web.multipart.MultipartFile;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    void updateProfile(Long userId, String displayName);

    void changePassword(Long userId, String currentPassword, String newPassword);

    String uploadAvatar(Long userId, MultipartFile file);
}
