package com.ktb.hackathon.repository;

import com.ktb.hackathon.entity.CounselingSession;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CounselingSessionRepository extends JpaRepository<CounselingSession, Long> {

	Optional<CounselingSession> findByIdAndChildProfileParentAccountId(Long id, Long parentAccountId);

	List<CounselingSession> findAllByChildProfileParentAccountIdOrderByCreatedAtDesc(Long parentAccountId);
}
