package com.collabdoc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.collabdoc.dto.workspace.InviteMemberRequest;
import com.collabdoc.dto.workspace.WorkspaceCreateRequest;
import com.collabdoc.dto.workspace.WorkspaceResponse;
import com.collabdoc.entity.User;
import com.collabdoc.entity.Workspace;
import com.collabdoc.entity.WorkspaceMember;
import com.collabdoc.enums.Role;
import com.collabdoc.exception.ResourceNotFoundException;
import com.collabdoc.exception.UnauthorizedException;
import com.collabdoc.repository.UserRepository;
import com.collabdoc.repository.WorkspaceMemberRepository;
import com.collabdoc.repository.WorkspaceRepository;
import com.collabdoc.service.WorkspaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkspaceServiceImpl implements WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository memberRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public WorkspaceResponse createWorkspace(WorkspaceCreateRequest request, Long userId) {
        Workspace workspace = Workspace.builder()
                .name(request.getName())
                .description(request.getDescription())
                .ownerId(userId)
                .build();
        workspaceRepository.insert(workspace);

        WorkspaceMember member = WorkspaceMember.builder()
                .workspaceId(workspace.getId())
                .userId(userId)
                .role(Role.OWNER)
                .build();
        memberRepository.insert(member);

        User owner = userRepository.selectById(userId);
        return toResponse(workspace, owner.getUsername(), 1);
    }

    @Override
    public List<WorkspaceResponse> getMyWorkspaces(Long userId) {
        List<Workspace> workspaces = workspaceRepository.findAllByUserId(userId);
        return workspaces.stream().map(w -> {
            User owner = userRepository.selectById(w.getOwnerId());
            int count = memberRepository.selectCount(
                    new LambdaQueryWrapper<WorkspaceMember>().eq(WorkspaceMember::getWorkspaceId, w.getId())).intValue();
            return toResponse(w, owner != null ? owner.getUsername() : "unknown", count);
        }).toList();
    }

    @Override
    public WorkspaceResponse getWorkspace(Long workspaceId, Long userId) {
        Workspace workspace = workspaceRepository.selectById(workspaceId);
        if (workspace == null) {
            throw new ResourceNotFoundException("Workspace not found");
        }
        checkMember(workspaceId, userId);
        User owner = userRepository.selectById(workspace.getOwnerId());
        int count = memberRepository.selectCount(
                new LambdaQueryWrapper<WorkspaceMember>().eq(WorkspaceMember::getWorkspaceId, workspaceId)).intValue();
        return toResponse(workspace, owner.getUsername(), count);
    }

    @Override
    @Transactional
    public void inviteMember(Long workspaceId, InviteMemberRequest request, Long inviterId) {
        checkAdminOrOwner(workspaceId, inviterId);

        User invitee = userRepository.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsernameOrEmail()));
        if (invitee == null) {
            invitee = userRepository.selectOne(
                    new LambdaQueryWrapper<User>().eq(User::getEmail, request.getUsernameOrEmail()));
        }
        if (invitee == null) {
            throw new ResourceNotFoundException("User not found");
        }

        Long exists = memberRepository.selectCount(
                new LambdaQueryWrapper<WorkspaceMember>()
                        .eq(WorkspaceMember::getWorkspaceId, workspaceId)
                        .eq(WorkspaceMember::getUserId, invitee.getId()));
        if (exists > 0) {
            throw new IllegalArgumentException("User is already a member");
        }

        WorkspaceMember member = WorkspaceMember.builder()
                .workspaceId(workspaceId)
                .userId(invitee.getId())
                .role(Role.MEMBER)
                .build();
        memberRepository.insert(member);
    }

    @Override
    @Transactional
    public void removeMember(Long workspaceId, Long targetUserId, Long requesterId) {
        checkAdminOrOwner(workspaceId, requesterId);
        memberRepository.delete(
                new LambdaQueryWrapper<WorkspaceMember>()
                        .eq(WorkspaceMember::getWorkspaceId, workspaceId)
                        .eq(WorkspaceMember::getUserId, targetUserId));
    }

    @Override
    public List<WorkspaceMember> getMembers(Long workspaceId, Long userId) {
        checkMember(workspaceId, userId);
        return memberRepository.selectList(
                new LambdaQueryWrapper<WorkspaceMember>().eq(WorkspaceMember::getWorkspaceId, workspaceId));
    }

    private void checkMember(Long workspaceId, Long userId) {
        Long count = memberRepository.selectCount(
                new LambdaQueryWrapper<WorkspaceMember>()
                        .eq(WorkspaceMember::getWorkspaceId, workspaceId)
                        .eq(WorkspaceMember::getUserId, userId));
        if (count == 0) {
            throw new UnauthorizedException("You are not a member of this workspace");
        }
    }

    private void checkAdminOrOwner(Long workspaceId, Long userId) {
        WorkspaceMember member = memberRepository.selectOne(
                new LambdaQueryWrapper<WorkspaceMember>()
                        .eq(WorkspaceMember::getWorkspaceId, workspaceId)
                        .eq(WorkspaceMember::getUserId, userId));
        if (member == null) {
            throw new UnauthorizedException("You are not a member of this workspace");
        }
        if (member.getRole() != Role.OWNER && member.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("Only owners and admins can perform this action");
        }
    }

    private WorkspaceResponse toResponse(Workspace workspace, String ownerName, int memberCount) {
        return WorkspaceResponse.builder()
                .id(workspace.getId())
                .name(workspace.getName())
                .description(workspace.getDescription())
                .ownerId(workspace.getOwnerId())
                .ownerName(ownerName)
                .memberCount(memberCount)
                .createdAt(workspace.getCreatedAt())
                .updatedAt(workspace.getUpdatedAt())
                .build();
    }
}
