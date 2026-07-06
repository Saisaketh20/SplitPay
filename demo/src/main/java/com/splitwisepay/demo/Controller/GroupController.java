package com.splitwisepay.demo.Controller;

import com.splitwisepay.demo.DTO.Request.CreateGroupRequest;
import com.splitwisepay.demo.DTO.Response.ApiResponse;
import com.splitwisepay.demo.DTO.Response.GroupResponse;
import com.splitwisepay.demo.Entity.User;
import com.splitwisepay.demo.Service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Controller/GroupController.java
@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    // ─── CREATE GROUP ─────────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<ApiResponse<GroupResponse>> createGroup(
            @Valid @RequestBody CreateGroupRequest request,
            @AuthenticationPrincipal User currentUser) {

        GroupResponse response = groupService.createGroup(request, currentUser);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Group created successfully", response));
    }

    // ─── GET MY GROUPS ────────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<ApiResponse<List<GroupResponse>>> getMyGroups(
            @AuthenticationPrincipal User currentUser) {

        List<GroupResponse> groups = groupService.getMyGroups(currentUser);
        return ResponseEntity.ok(ApiResponse.success(groups));
    }

    // ─── GET GROUP BY ID ──────────────────────────────────────────────
    @GetMapping("/{groupId}")
    public ResponseEntity<ApiResponse<GroupResponse>> getGroupById(
            @PathVariable Long groupId,
            @AuthenticationPrincipal User currentUser) {

        GroupResponse response = groupService.getGroupById(groupId, currentUser);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ─── ADD MEMBER ───────────────────────────────────────────────────
    @PostMapping("/{groupId}/members")
    public ResponseEntity<ApiResponse<GroupResponse>> addMember(
            @PathVariable Long groupId,
            @RequestParam Long userId,
            @AuthenticationPrincipal User currentUser) {

        GroupResponse response = groupService.addMember(groupId, userId, currentUser);
        return ResponseEntity.ok(
                ApiResponse.success("Member added successfully", response));
    }

    // ─── REMOVE MEMBER ────────────────────────────────────────────────
    @DeleteMapping("/{groupId}/members/{userId}")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable Long groupId,
            @PathVariable Long userId,
            @AuthenticationPrincipal User currentUser) {

        groupService.removeMember(groupId, userId, currentUser);
        return ResponseEntity.ok(
                ApiResponse.error("Member removed successfully"));
    }

    // ─── DELETE GROUP ─────────────────────────────────────────────────
    @DeleteMapping("/{groupId}")
    public ResponseEntity<ApiResponse<Void>> deleteGroup(
            @PathVariable Long groupId,
            @AuthenticationPrincipal User currentUser) {

        groupService.deleteGroup(groupId, currentUser);
        return ResponseEntity.ok(
                ApiResponse.success("Group deleted successfully", null));
    }
}