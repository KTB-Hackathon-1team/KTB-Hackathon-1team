package com.ktb.hackathon.dto.response;

import com.ktb.hackathon.entity.ConversationMessage;
import com.ktb.hackathon.entity.enums.MessageSpeaker;

public record ConversationTurnResponse(
	String role,
	String text
) {

	public static ConversationTurnResponse from(ConversationMessage message) {
		return new ConversationTurnResponse(
			toRole(message.getSpeaker()),
			message.getContent()
		);
	}

	private static String toRole(MessageSpeaker speaker) {
		return speaker == MessageSpeaker.CHILD ? "user" : "assistant";
	}
}
