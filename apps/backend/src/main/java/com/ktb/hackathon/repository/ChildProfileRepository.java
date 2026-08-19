package com.ktb.hackathon.repository;

import com.ktb.hackathon.entity.ChildProfile;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChildProfileRepository extends JpaRepository<ChildProfile, Long> {

	List<ChildProfile> findAllByParentAccountIdOrderByCreatedAtAsc(Long parentAccountId);
}
