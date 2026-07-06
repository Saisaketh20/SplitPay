package com.splitwisepay.demo.Repository;

import com.splitwisepay.demo.Entity.Group;
import com.splitwisepay.demo.Entity.GroupMember;
import com.splitwisepay.demo.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    Optional<GroupMember> findByGroupAndUser(Group group, User user);
    boolean existsByGroupAndUser(Group group, User user);
    List<GroupMember> findByUser(User user);
    List<GroupMember>findByGroup(Group group);
}