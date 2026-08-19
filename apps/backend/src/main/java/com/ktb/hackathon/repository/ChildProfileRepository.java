package com.ktb.hackathon.repository;

import com.ktb.hackathon.entity.ChildProfile;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChildProfileRepository extends JpaRepository<ChildProfile, Long> {

	Optional<ChildProfile> findByIdAndParentAccountId(Long id, Long parentAccountId);

	List<ChildProfile> findAllByParentAccountIdOrderByCreatedAtAsc(Long parentAccountId);
}
