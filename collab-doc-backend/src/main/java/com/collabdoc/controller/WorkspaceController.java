package com.collabdoc.controller;

import com.collabdoc.dto.common.ApiResponse;
import com.collabdoc.dto.workspace.InviteMemberRequest;
import com.collabdoc.dto.workspace.WorkspaceCreateRequest;
import com.collabdoc.dto.workspace.WorkspaceResponse;
import com.collabdoc.entity.WorkspaceMember;
import com.collabdoc.service.WorkspaceService;
import com.collabdoc.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @PostMapping
    public ResponseEntity<ApiResponse<WorkspaceResponse>> create(
            @Valid @RequestBody WorkspaceCreateRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        WorkspaceResponse response = workspaceService.createWorkspace(request, userId);
        return ResponseEntity.ok(ApiResponse.ok(response, "Workspace created"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<WorkspaceResponse>>> list() {
        Long userId = SecurityUtil.getCurrentUserId();
        List<WorkspaceResponse> workspaces = workspaceService.getMyWorkspaces(userId);
        return ResponseEntity.ok(ApiResponse.ok(workspaces));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkspaceResponse>> get(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        WorkspaceResponse response = workspaceService.getWorkspace(id, userId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<ApiResponse<Void>> inviteMember(
            @PathVariable Long id,
            @Valid @RequestBody InviteMemberRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        workspaceService.inviteMember(id, request, userId);
        return ResponseEntity.ok(ApiResponse.ok(null, "Member invited"));
    }

    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable Long id,
            @PathVariable Long userId) {
        Long requesterId = SecurityUtil.getCurrentUserId();
        workspaceService.removeMember(id, userId, requesterId);
        return ResponseEntity.ok(ApiResponse.ok(null, "Member removed"));
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<ApiResponse<List<WorkspaceMember>>> getMembers(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        List<WorkspaceMember> members = workspaceService.getMembers(id, userId);
        return ResponseEntity.ok(ApiResponse.ok(members));
    }
}
