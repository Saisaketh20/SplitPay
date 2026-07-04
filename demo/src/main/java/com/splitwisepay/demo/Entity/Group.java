package com.splitwisepay.demo.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "split_groups")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    // FIX: field name is 'createdBy' not 'created_by'
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(nullable = false)
    private String currency = "INR";

    @Column(nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    private LocalDateTime createdAt;

    // FIX: mappedBy must match the exact field name in GroupMember.java
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    @ToString.Exclude   // prevents Lombok infinite loop between Group <-> GroupMember
    private List<GroupMember> members;

    // FIX: mappedBy = "group" matches the field name in Expense.java
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<Expense> expenses;

    // FIX: mappedBy = "group" matches the field name in Settlement.java
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<Settlement> settlements;
}