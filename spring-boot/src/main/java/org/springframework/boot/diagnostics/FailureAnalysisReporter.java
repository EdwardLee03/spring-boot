
package org.springframework.boot.diagnostics;

/**
 * Reports a {@code FailureAnalysis} to the user.
 * 故障分析结果的报告者。
 *
 * @author Andy Wilkinson
 * @since 1.4.0
 */
public interface FailureAnalysisReporter {

	/**
	 * Reports the given {@code failureAnalysis} to the user.
	 * 向用户报告给定故障的分析结果。
	 * @param analysis the analysis
	 */
	void report(FailureAnalysis analysis);

}
