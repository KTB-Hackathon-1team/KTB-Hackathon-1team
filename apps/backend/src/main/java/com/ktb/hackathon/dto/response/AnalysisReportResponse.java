package com.ktb.hackathon.dto.response;

import com.ktb.hackathon.entity.AnalysisReport;

public record AnalysisReportResponse(
	String summary,
	String emotionSummary,
	String parentingGuidance,
	String resultPayload,
	String modelName,
	String promptVersion
) {

	public static AnalysisReportResponse from(AnalysisReport analysisReport) {
		if (analysisReport == null) {
			return null;
		}

		return new AnalysisReportResponse(
			analysisReport.getSummary(),
			analysisReport.getEmotionSummary(),
			analysisReport.getParentingGuidance(),
			analysisReport.getResultPayload(),
			analysisReport.getModelName(),
			analysisReport.getPromptVersion()
		);
	}
}
