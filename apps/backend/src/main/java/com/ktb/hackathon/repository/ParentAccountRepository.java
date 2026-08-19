package com.ktb.hackathon.repository;

import com.ktb.hackathon.entity.ParentAccount;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParentAccountRepository extends JpaRepository<ParentAccount, Long> {

	Optional<ParentAccount> findByLoginId(String loginId);

	boolean existsByLoginId(String loginId);
}
