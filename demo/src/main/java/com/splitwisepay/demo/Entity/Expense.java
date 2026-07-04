package com.splitwisepay.demo.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "expenses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FIX: field named 'group' not 'groupId' — it holds an object, not a raw ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    @ToString.Exclude
    private Group group;

    // FIX: field named 'paidBy' not 'userId' — clear and accurate name
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paid_by", nullable = false)
    private User paidBy;

    // FIX: mappedBy = "expense" matches field name in ExpenseSplit.java
    @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<ExpenseSplit> splits;

    // FIX: BigDecimal for money — never use long/double for currency
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SplitType splitType;

    public enum SplitType {
        EQUAL, PERCENT, EXACT
    }

    // FIX: LocalDate instead of java.util.Date
    private LocalDate expenseDate;

    @CreationTimestamp
    private LocalDateTime createdAt;
}