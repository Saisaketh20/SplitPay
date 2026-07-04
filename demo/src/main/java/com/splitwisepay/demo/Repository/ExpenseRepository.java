package com.splitwisepay.demo.Repository;
import com.splitwisepay.demo.Entity.Expense;
import com.splitwisepay.demo.Entity.Group;
import com.splitwisepay.demo.Entity.GroupMember;
import com.splitwisepay.demo.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;



@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByGroup(Group group);
}
