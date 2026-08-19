package com.ktb.hackathon.repository;

import com.ktb.hackathon.entity.ConversationMessage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationMessageRepository extends JpaRepository<ConversationMessage, Long> {

	List<ConversationMessage> findAllByCounselingSessionIdOrderBySequenceNoAsc(Long counselingSessionId);
}
