package com.splitwisepay.demo.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "group_members",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"group_id", "user_id"})
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FIX: field name is 'group' — mappedBy = "group" in Group.java matches this
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    @ToString.Exclude
    private Group group;

    // FIX: was @OneToOne — WRONG. One user can be in many groups. Must be @ManyToOne
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.MEMBER;

    public enum Role {
        ADMIN, MEMBER
    }

    @CreationTimestamp
    private LocalDateTime joinedAt;
}