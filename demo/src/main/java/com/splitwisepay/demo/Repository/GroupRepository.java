package com.splitwisepay.demo.Repository;

import com.splitwisepay.demo.Entity.Group;
import com.splitwisepay.demo.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {


     List<Group> findByCreatedBy(User user);
}


