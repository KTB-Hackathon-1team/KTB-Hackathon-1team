package com.ktb.hackathon.dto.response;

import com.ktb.hackathon.entity.ConversationMessage;
import java.util.List;
import java.util.stream.Collectors;

public record ConversationResponse(
	List<ConversationTurnResponse> turns,
	String text
) {

	public static ConversationResponse from(List<ConversationMessage> messages) {
		List<ConversationTurnResponse> turns = messages.stream()
			.map(ConversationTurnResponse::from)
			.toList();

		String text = turns.stream()
			.map(turn -> ("user".equals(turn.role()) ? "아이: " : "에이전트: ") + turn.text())
			.collect(Collectors.joining("\n"));

		return new ConversationResponse(turns, text);
	}
}
