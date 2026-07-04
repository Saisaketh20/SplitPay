package com.splitwisepay.demo.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "expense_splits")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseSplit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FIX: field named 'expense' not 'expenseId' — mappedBy = "expense" in Expense.java
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id", nullable = false)
    @ToString.Exclude
    private Expense expense;

    // FIX: field named 'user' not 'userId'
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // FIX: camelCase field names + BigDecimal for money
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amountOwed;



    // FIX: camelCase
    @Column(nullable = false)
    private Boolean isSettled = false;
}