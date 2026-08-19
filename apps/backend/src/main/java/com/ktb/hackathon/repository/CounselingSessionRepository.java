package com.ktb.hackathon.repository;

import com.ktb.hackathon.entity.CounselingSession;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CounselingSessionRepository extends JpaRepository<CounselingSession, Long> {

	Optional<CounselingSession> findByIdAndChildProfileParentAccountId(Long id, Long parentAccountId);

	List<CounselingSession> findAllByChildProfileParentAccountIdOrderByCreatedAtDesc(Long parentAccountId);

	Optional<CounselingSession> findByIdAndChildProfileIdAndChildProfileParentAccountId(
		Long sessionId,
		Long childProfileId,
		Long parentAccountId
	);

	List<CounselingSession> findAllByChildProfileIdAndChildProfileParentAccountIdOrderByIdDesc(
		Long childProfileId,
		Long parentAccountId,
		Pageable pageable
	);

	List<CounselingSession> findAllByChildProfileIdAndChildProfileParentAccountIdOrderByIdDesc(
		Long childProfileId,
		Long parentAccountId
	);

	List<CounselingSession> findAllByChildProfileIdAndChildProfileParentAccountIdAndIdLessThanOrderByIdDesc(
		Long childProfileId,
		Long parentAccountId,
		Long cursorId,
		Pageable pageable
	);
}
