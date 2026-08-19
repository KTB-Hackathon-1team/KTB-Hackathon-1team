package com.ktb.hackathon.repository;

import com.ktb.hackathon.entity.Recording;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecordingRepository extends JpaRepository<Recording, Long> {

	Optional<Recording> findByCounselingSessionId(Long counselingSessionId);
}
