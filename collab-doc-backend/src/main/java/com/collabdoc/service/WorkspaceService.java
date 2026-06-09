package com.collabdoc.service;

import com.collabdoc.dto.workspace.InviteMemberRequest;
import com.collabdoc.dto.workspace.WorkspaceCreateRequest;
import com.collabdoc.dto.workspace.WorkspaceResponse;
import com.collabdoc.entity.WorkspaceMember;

import java.util.List;

public interface WorkspaceService {

    WorkspaceResponse createWorkspace(WorkspaceCreateRequest request, Long userId);

    List<WorkspaceResponse> getMyWorkspaces(Long userId);

    WorkspaceResponse getWorkspace(Long workspaceId, Long userId);

    void inviteMember(Long workspaceId, InviteMemberRequest request, Long inviterId);

    void removeMember(Long workspaceId, Long targetUserId, Long requesterId);

    List<WorkspaceMember> getMembers(Long workspaceId, Long userId);
}
