package com.collabdoc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.collabdoc.config.JwtUtil;
import com.collabdoc.dto.auth.AuthResponse;
import com.collabdoc.dto.auth.LoginRequest;
import com.collabdoc.dto.auth.RegisterRequest;
import com.collabdoc.entity.User;
import com.collabdoc.repository.UserRepository;
import com.collabdoc.service.AuthService;
import com.collabdoc.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final FileStorageService fileStorageService;

    @Override
    public AuthResponse register(RegisterRequest request) {
        Long countByUsername = userRepository.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()));
        if (countByUsername > 0) {
            throw new IllegalArgumentException("Username already taken");
        }

        Long countByEmail = userRepository.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getEmail, request.getEmail()));
        if (countByEmail > 0) {
            throw new IllegalArgumentException("Email already registered");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .displayName(request.getUsername())
                .build();
        userRepository.insert(user);

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        return new AuthResponse(token, user.getId(), user.getUsername(), user.getEmail(), user.getDisplayName(), user.getAvatarUrl());
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        User user = userRepository.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()));
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        return new AuthResponse(token, user.getId(), user.getUsername(), user.getEmail(), user.getDisplayName(), user.getAvatarUrl());
    }

    @Override
    public void updateProfile(Long userId, String displayName) {
        User user = userRepository.selectById(userId);
        if (user == null) throw new UsernameNotFoundException("User not found");
        if (displayName != null) {
            user.setDisplayName(displayName);
        }
        userRepository.updateById(user);
    }

    @Override
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.selectById(userId);
        if (user == null) throw new UsernameNotFoundException("User not found");

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.updateById(user);
    }

    @Override
    public String uploadAvatar(Long userId, MultipartFile file) {
        User user = userRepository.selectById(userId);
        if (user == null) throw new UsernameNotFoundException("User not found");

        String objectKey = fileStorageService.uploadFile(file, 0L);
        String url = fileStorageService.getFileUrl(objectKey);

        user.setAvatarUrl(url);
        userRepository.updateById(user);
        return url;
    }
}
