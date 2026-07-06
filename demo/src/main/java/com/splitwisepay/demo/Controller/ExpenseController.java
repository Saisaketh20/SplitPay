package com.splitwisepay.demo.Controller;

import com.splitwisepay.demo.DTO.Request.AddExpenseRequest;
import com.splitwisepay.demo.DTO.Request.SettleRequest;
import com.splitwisepay.demo.DTO.Response.ApiResponse;
import com.splitwisepay.demo.DTO.Response.BalanceResponse;
import com.splitwisepay.demo.DTO.Response.ExpenseResponse;
import com.splitwisepay.demo.Entity.User;
import com.splitwisepay.demo.Service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Controller/ExpenseController.java
@RestController
@RequestMapping("/api/v1/groups/{groupId}/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    // ─── ADD EXPENSE ──────────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<ApiResponse<ExpenseResponse>> addExpense(
            @PathVariable Long groupId,
            @Valid @RequestBody AddExpenseRequest request,
            @AuthenticationPrincipal User currentUser) {

        ExpenseResponse response = expenseService
                .addExpense(groupId, request, currentUser);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Expense added successfully", response));
    }

    // ─── GET ALL EXPENSES ─────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<ApiResponse<List<ExpenseResponse>>> getGroupExpenses(
            @PathVariable Long groupId,
            @AuthenticationPrincipal User currentUser) {

        List<ExpenseResponse> expenses = expenseService
                .getGroupExpenses(groupId, currentUser);
        return ResponseEntity.ok(ApiResponse.success(expenses));
    }

    // ─── GET SINGLE EXPENSE ───────────────────────────────────────────
    @GetMapping("/{expenseId}")
    public ResponseEntity<ApiResponse<ExpenseResponse>> getExpenseById(
            @PathVariable Long groupId,
            @PathVariable Long expenseId,
            @AuthenticationPrincipal User currentUser) {

        ExpenseResponse response = expenseService
                .getExpenseById(groupId, expenseId, currentUser);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ─── DELETE EXPENSE ───────────────────────────────────────────────
    @DeleteMapping("/{expenseId}")
    public ResponseEntity<ApiResponse<Void>> deleteExpense(
            @PathVariable Long groupId,
            @PathVariable Long expenseId,
            @AuthenticationPrincipal User currentUser) {

        expenseService.deleteExpense(groupId, expenseId, currentUser);
        return ResponseEntity.ok(
                ApiResponse.success("Expense deleted successfully", null));
    }

    // ─── GET BALANCES ─────────────────────────────────────────────────
    @GetMapping("/balances")
    public ResponseEntity<ApiResponse<List<BalanceResponse>>> getBalances(
            @PathVariable Long groupId,
            @AuthenticationPrincipal User currentUser) {

        List<BalanceResponse> balances = expenseService
                .getGroupBalances(groupId, currentUser);
        return ResponseEntity.ok(ApiResponse.success(balances));
    }

    // ─── SETTLE UP ────────────────────────────────────────────────────
    @PostMapping("/settle")
    public ResponseEntity<ApiResponse<Void>> settleUp(
            @PathVariable Long groupId,
            @Valid @RequestBody SettleRequest request,
            @AuthenticationPrincipal User currentUser) {

        expenseService.settleUp(groupId, request, currentUser);
        return ResponseEntity.ok(
                ApiResponse.success("Settlement recorded successfully", null));
    }
}