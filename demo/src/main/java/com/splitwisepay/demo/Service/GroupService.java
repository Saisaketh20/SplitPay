package com.splitwisepay.demo.Service;

import com.splitwisepay.demo.DTO.Request.CreateGroupRequest;
import com.splitwisepay.demo.DTO.Response.GroupResponse;
import com.splitwisepay.demo.DTO.Response.MemberResponse;
import com.splitwisepay.demo.DTO.Response.UserResponse;
import com.splitwisepay.demo.Entity.Group;
import com.splitwisepay.demo.Entity.GroupMember;
import com.splitwisepay.demo.Entity.User;
import com.splitwisepay.demo.Repository.GroupMemberRepository;
import com.splitwisepay.demo.Repository.GroupRepository;
import com.splitwisepay.demo.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupService {
   private final GroupRepository groupRepository;
   private final GroupMemberRepository groupMemberRepository;
   private final UserRepository userRepository;

   public GroupResponse createGroup(CreateGroupRequest request,User currentUser){
       Group group = Group.builder()
               .name(request.getName())
               .description(request.getDescription())
               .currency(request.getCurrency() != null ? request.getCurrency() : "INR")
               .createdBy(currentUser)
               .isActive(true)
               .build();
       Group savedGroup = groupRepository.save(group);

       // 2. Auto-add creator as group ADMIN member
       GroupMember creatorMember = GroupMember.builder()
               .group(savedGroup)
               .user(currentUser)
               .role(GroupMember.Role.ADMIN)
               .build();

       groupMemberRepository.save(creatorMember);

       return mapToGroupResponse(savedGroup);
   }
    // Converts Group entity → GroupResponse DTO
    // Never expose entity directly to controller
    private GroupResponse mapToGroupResponse(Group group) {

        List<MemberResponse> members = groupMemberRepository
                .findByGroup(group)  // ← add this method to GroupMemberRepository
                .stream()
                .map(member -> MemberResponse.builder()
                        .userId(member.getUser().getId())
                        .name(member.getUser().getName())
                        .email(member.getUser().getEmail())
                        .role(member.getRole().name())
                        .build())
                .collect(Collectors.toList());

        return GroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .currency(group.getCurrency())
                .createdBy(UserResponse.builder()
                        .id(group.getCreatedBy().getId())
                        .name(group.getCreatedBy().getName())
                        .email(group.getCreatedBy().getEmail())
                        .role(group.getCreatedBy().getRole().name())
                        .build())
                .members(members)
                .createdAt(group.getCreatedAt())
                .build();
    }

    public List<GroupResponse> getMyGroups(User currentUser) {

        // Find all group_members rows for this user
        List<GroupMember> memberships = groupMemberRepository
                .findByUser(currentUser);

        return memberships.stream()
                .map(GroupMember::getGroup)
                .filter(Group::getIsActive)
                .map(this::mapToGroupResponse)
                .collect(Collectors.toList());
    }

    public GroupResponse getGroupById(Long groupId, User currentUser) {

        Group group = findGroupById(groupId);

        // Only members can view the group
        validateMembership(group, currentUser);

        return mapToGroupResponse(group);
    }

    // ─── ADD MEMBER TO GROUP ──────────────────────────────────────────
    public GroupResponse addMember(Long groupId, Long userId, User currentUser) {

        Group group = findGroupById(groupId);

        // Only group ADMIN can add members
        validateGroupAdmin(group, currentUser);

        // Find the user to add
        User userToAdd = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(
                        "User not found with id: " + userId));

        // Check if already a member
        if (groupMemberRepository.existsByGroupAndUser(group, userToAdd)) {
            throw new RuntimeException("User is already a member of this group");
        }

        GroupMember newMember = GroupMember.builder()
                .group(group)
                .user(userToAdd)
                .role(GroupMember.Role.MEMBER)
                .build();

        groupMemberRepository.save(newMember);

        return mapToGroupResponse(group);
    }

    // ─── REMOVE MEMBER FROM GROUP ─────────────────────────────────────
    public void removeMember(Long groupId, Long userId, User currentUser) {

        Group group = findGroupById(groupId);

        // Only group ADMIN can remove members
        validateGroupAdmin(group, currentUser);

        // Cannot remove yourself if you are the only admin
        if (currentUser.getId().equals(userId)) {
            throw new RuntimeException(
                    "Group admin cannot remove themselves");
        }

        User userToRemove = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(
                        "User not found with id: " + userId));

        GroupMember member = groupMemberRepository
                .findByGroupAndUser(group, userToRemove)
                .orElseThrow(() -> new RuntimeException(
                        "User is not a member of this group"));

        groupMemberRepository.delete(member);
    }

    public void deleteGroup(Long groupId, User currentUser) {

        Group group = findGroupById(groupId);

        // Only group ADMIN can delete
        validateGroupAdmin(group, currentUser);

        // Soft delete — set isActive = false, don't delete from DB
        group.setIsActive(false);
        groupRepository.save(group);
    }

    // ─── PRIVATE HELPER METHODS ───────────────────────────────────────

    private Group findGroupById(Long groupId) {
        return groupRepository.findById(groupId)
                .filter(Group::getIsActive)
                .orElseThrow(() -> new RuntimeException(
                        "Group not found with id: " + groupId));
    }

    // Checks if user is a member of the group at all
    private void validateMembership(Group group, User user) {
        if (!groupMemberRepository.existsByGroupAndUser(group, user)) {
            throw new RuntimeException(
                    "You are not a member of this group");
        }
    }

    private void validateGroupAdmin(Group group, User user) {
        GroupMember member = groupMemberRepository
                .findByGroupAndUser(group, user)
                .orElseThrow(() -> new RuntimeException(
                        "You are not a member of this group"));

        if (member.getRole() != GroupMember.Role.ADMIN) {
            throw new RuntimeException(
                    "Only group admin can perform this action");
        }
    }
}
