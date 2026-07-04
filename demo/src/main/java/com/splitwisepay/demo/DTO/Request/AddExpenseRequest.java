package com.splitwisepay.demo.DTO.Request;

import com.splitwisepay.demo.Entity.Expense;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.*;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

// dto/request/AddExpenseRequest.java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddExpenseRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Split type is required")
    private Expense.SplitType splitType;

    private LocalDate expenseDate;

    // List of user IDs to split with
    @NotEmpty(message = "At least one split member required")
    private List<Long> splitMemberIds;

    // Only used when splitType = EXACT or PERCENT
    // Key = userId, Value = amount or percentage
    private Map<Long, BigDecimal> splitValues;
}
