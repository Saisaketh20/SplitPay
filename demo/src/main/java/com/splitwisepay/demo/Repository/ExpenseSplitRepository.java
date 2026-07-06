package com.splitwisepay.demo.Repository;

import com.splitwisepay.demo.Entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseSplitRepository extends JpaRepository<ExpenseSplit, Long> {
    List<ExpenseSplit> findByUserAndIsSettled(User user, Boolean isSettled);
    List<ExpenseSplit> findByExpense(Expense expense);

    void deleteByExpense(Expense expense);

    @Query("SELECT es FROM ExpenseSplit es " +
            "WHERE es.user = :payer " +
            "AND es.expense.paidBy = :payee " +
            "AND es.expense.group = :group " +
            "AND es.isSettled = false")
    List<ExpenseSplit> findUnsettledSplitsBetweenUsers(
            @Param("payer") User payer,
            @Param("payee") User payee,
            @Param("group") Group group);
}