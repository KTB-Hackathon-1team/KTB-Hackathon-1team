package com.ktb.hackathon.dto.response;

import java.util.List;

public record CounselingSessionListResponse(
	List<CounselingSessionResponse> items,
	Long nextCursorId,
	boolean hasNext
) {
}
