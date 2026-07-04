package com.splitwisepay.demo.DTO.Response;

import com.splitwisepay.demo.Entity.GroupMember;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupResponse {
    private Long id;
    private String name;
    private String description;
    private String currency;
    private UserResponse createdBy;
    private List<MemberResponse> members;
    private LocalDateTime createdAt;
}
