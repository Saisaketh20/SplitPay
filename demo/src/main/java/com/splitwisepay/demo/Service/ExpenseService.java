package com.splitwisepay.demo.Service;

import com.splitwisepay.demo.DTO.Request.AddExpenseRequest;
import com.splitwisepay.demo.DTO.Request.SettleRequest;
import com.splitwisepay.demo.DTO.Response.BalanceResponse;
import com.splitwisepay.demo.DTO.Response.ExpenseResponse;
import com.splitwisepay.demo.DTO.Response.UserResponse;
import com.splitwisepay.demo.Entity.*;
import com.splitwisepay.demo.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// Service/ExpenseService.java
@Service
@RequiredArgsConstructor
@Transactional
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseSplitRepository expenseSplitRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;

    // ─── ADD EXPENSE ──────────────────────────────────────────────────
    public ExpenseResponse addExpense(Long groupId,
                                      AddExpenseRequest request,
                                      User currentUser) {

        // 1. Find group and validate current user is a member
        Group group = findActiveGroup(groupId);
        validateMembership(group, currentUser);

        // 2. Build and save the expense
        Expense expense = Expense.builder()
                .group(group)
                .paidBy(currentUser)
                .amount(request.getAmount())
                .description(request.getDescription())
                .splitType(request.getSplitType())
                .expenseDate(request.getExpenseDate() != null
                        ? request.getExpenseDate()
                        : LocalDate.now())
                .build();

        Expense savedExpense = expenseRepository.save(expense);

        // 3. Calculate and save splits
        saveSplits(savedExpense, request);

        return mapToExpenseResponse(savedExpense);
    }

    // ─── GET ALL EXPENSES FOR A GROUP ────────────────────────────────
    public List<ExpenseResponse> getGroupExpenses(Long groupId, User currentUser) {

        Group group = findActiveGroup(groupId);
        validateMembership(group, currentUser);

        return expenseRepository.findByGroup(group)
                .stream()
                .map(this::mapToExpenseResponse)
                .collect(Collectors.toList());
    }

    // ─── GET SINGLE EXPENSE ───────────────────────────────────────────
    public ExpenseResponse getExpenseById(Long groupId,
                                          Long expenseId,
                                          User currentUser) {

        Group group = findActiveGroup(groupId);
        validateMembership(group, currentUser);

        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new RuntimeException(
                        "Expense not found with id: " + expenseId));

        return mapToExpenseResponse(expense);
    }

    // ─── DELETE EXPENSE ───────────────────────────────────────────────
    public void deleteExpense(Long groupId, Long expenseId, User currentUser) {

        Group group = findActiveGroup(groupId);
        validateMembership(group, currentUser);

        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new RuntimeException(
                        "Expense not found with id: " + expenseId));

        // Only the person who added it can delete it
        if (!expense.getPaidBy().getId().equals(currentUser.getId())) {
            throw new RuntimeException(
                    "Only the person who added this expense can delete it");
        }

        // Delete splits first (foreign key constraint)
        expenseSplitRepository.deleteByExpense(expense);
        expenseRepository.delete(expense);
    }

    // ─── GET BALANCES FOR A GROUP ─────────────────────────────────────
    // This is the most important method — interviewers WILL ask about this
    public List<BalanceResponse> getGroupBalances(Long groupId, User currentUser) {

        Group group = findActiveGroup(groupId);
        validateMembership(group, currentUser);

        // Get all members of the group
        List<GroupMember> members = groupMemberRepository.findByGroup(group);

        // For each member, calculate net balance
        // netBalance > 0 means others owe them
        // netBalance < 0 means they owe others
        Map<Long, BigDecimal> balanceMap = new HashMap<>();

        // Initialize all members with 0 balance
        members.forEach(m ->
                balanceMap.put(m.getUser().getId(), BigDecimal.ZERO));

        // Get all expenses for this group
        List<Expense> expenses = expenseRepository.findByGroup(group);

        for (Expense expense : expenses) {
            Long paidById = expense.getPaidBy().getId();
            BigDecimal totalAmount = expense.getAmount();

            // Person who paid gets credit for full amount
            balanceMap.merge(paidById, totalAmount, BigDecimal::add);

            // Each split member gets debited their share
            List<ExpenseSplit> splits = expenseSplitRepository
                    .findByExpense(expense);

            for (ExpenseSplit split : splits) {
                if (!split.getIsSettled()) {
                    Long userId = split.getUser().getId();
                    balanceMap.merge(userId,
                            split.getAmountOwed().negate(), // subtract
                            BigDecimal::add);
                }
            }
        }

        // Convert map to response list
        return members.stream()
                .map(member -> {
                    User user = member.getUser();
                    return BalanceResponse.builder()
                            .userId(user.getId())
                            .userName(user.getName())
                            .netBalance(balanceMap.getOrDefault(
                                    user.getId(), BigDecimal.ZERO))
                            .build();
                })
                .collect(Collectors.toList());
    }

    // ─── SETTLE UP ────────────────────────────────────────────────────
    public void settleUp(Long groupId, SettleRequest request, User currentUser) {

        Group group = findActiveGroup(groupId);
        validateMembership(group, currentUser);

        User paidTo = userRepository.findById(request.getPaidToUserId())
                .orElseThrow(() -> new RuntimeException(
                        "User not found with id: " + request.getPaidToUserId()));

        // Find unsettled splits where currentUser owes paidTo
        List<ExpenseSplit> unsettledSplits = expenseSplitRepository
                .findUnsettledSplitsBetweenUsers(
                        currentUser, paidTo, group);

        BigDecimal remaining = request.getAmount();

        // Mark splits as settled one by one until amount is used up
        for (ExpenseSplit split : unsettledSplits) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;

            if (remaining.compareTo(split.getAmountOwed()) >= 0) {
                // Full split settled
                split.setIsSettled(true);
                remaining = remaining.subtract(split.getAmountOwed());
            } else {
                // Partial — reduce the amount owed
                split.setAmountOwed(split.getAmountOwed().subtract(remaining));
                remaining = BigDecimal.ZERO;
            }
            expenseSplitRepository.save(split);
        }
    }

    // ─── PRIVATE HELPERS ──────────────────────────────────────────────

    private void saveSplits(Expense expense, AddExpenseRequest request) {

        List<Long> memberIds = request.getSplitMemberIds();
        int memberCount = memberIds.size();

        switch (expense.getSplitType()) {

            case EQUAL -> {
                // Split equally — divide total by number of members
                BigDecimal share = expense.getAmount()
                        .divide(BigDecimal.valueOf(memberCount),
                                2, RoundingMode.HALF_UP);

                memberIds.forEach(userId -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException(
                                    "User not found: " + userId));

                    // Person who paid is not considered as owing themselves
                    boolean isSettled = user.getId()
                            .equals(expense.getPaidBy().getId());

                    expenseSplitRepository.save(ExpenseSplit.builder()
                            .expense(expense)
                            .user(user)
                            .amountOwed(share)
                            .isSettled(isSettled)
                            .build());
                });
            }

            case EXACT -> {
                // Each member owes a specific exact amount
                if (request.getSplitValues() == null) {
                    throw new RuntimeException(
                            "splitValues required for EXACT split type");
                }

                request.getSplitValues().forEach((userId, amount) -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException(
                                    "User not found: " + userId));

                    boolean isSettled = user.getId()
                            .equals(expense.getPaidBy().getId());

                    expenseSplitRepository.save(ExpenseSplit.builder()
                            .expense(expense)
                            .user(user)
                            .amountOwed(amount)
                            .isSettled(isSettled)
                            .build());
                });
            }

            case PERCENT -> {
                // Each member owes a percentage of total
                if (request.getSplitValues() == null) {
                    throw new RuntimeException(
                            "splitValues required for PERCENT split type");
                }

                request.getSplitValues().forEach((userId, percent) -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException(
                                    "User not found: " + userId));

                    BigDecimal amount = expense.getAmount()
                            .multiply(percent)
                            .divide(BigDecimal.valueOf(100),
                                    2, RoundingMode.HALF_UP);

                    boolean isSettled = user.getId()
                            .equals(expense.getPaidBy().getId());

                    expenseSplitRepository.save(ExpenseSplit.builder()
                            .expense(expense)
                            .user(user)
                            .amountOwed(amount)
                            .isSettled(isSettled)
                            .build());
                });
            }
        }
    }

    private Group findActiveGroup(Long groupId) {
        return groupRepository.findById(groupId)
                .filter(Group::getIsActive)
                .orElseThrow(() -> new RuntimeException(
                        "Group not found with id: " + groupId));
    }

    private void validateMembership(Group group, User user) {
        if (!groupMemberRepository.existsByGroupAndUser(group, user)) {
            throw new RuntimeException("You are not a member of this group");
        }
    }

    private ExpenseResponse mapToExpenseResponse(Expense expense) {
        return ExpenseResponse.builder()
                .id(expense.getId())
                .description(expense.getDescription())
                .amount(expense.getAmount())
                .splitType(expense.getSplitType().name())
                .paidBy(UserResponse.builder()
                        .id(expense.getPaidBy().getId())
                        .name(expense.getPaidBy().getName())
                        .email(expense.getPaidBy().getEmail())
                        .role(expense.getPaidBy().getRole().name())
                        .build())
                .expenseDate(expense.getExpenseDate())
                .createdAt(expense.getCreatedAt())
                .build();
    }
}