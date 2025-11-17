package com.jpmc.midascore.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jpmc.midascore.entity.UserAccount;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    Optional<UserAccount> findByName(String name);
}
