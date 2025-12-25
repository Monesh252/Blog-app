package com.spring.postify.repository;

import com.spring.postify.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("select u from User u where email = :email")
    User findUserByEmail(String email);
}