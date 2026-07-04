package com.splitwisepay.demo.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "settlements")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Settlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FIX: field named 'group' not 'groupId'
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    @ToString.Exclude
    private Group group;

    // FIX: camelCase field names
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paid_by", nullable = false)
    private User paidBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paid_to", nullable = false)
    private User paidTo;

    // FIX: BigDecimal for money
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    // FIX: LocalDateTime + @CreationTimestamp instead of raw Timestamp
    @CreationTimestamp
    private LocalDateTime settledAt;
}