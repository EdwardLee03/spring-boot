
package org.springframework.boot.diagnostics;

/**
 * A {@code FailureAnalyzer} is used to analyze a failure and provide diagnostic
 * information that can be displayed to the user.
 * 故障分析器，用于分析故障并提供诊断信息。
 *
 * @author Andy Wilkinson
 * @since 1.4.0
 */
public interface FailureAnalyzer {

	/**
	 * Returns an analysis of the given {@code failure}, or {@code null} if no analysis
	 * was possible.
	 * 返回给定故障的分析结果。
	 * @param failure the failure
	 * @return the analysis or {@code null}
	 */
	FailureAnalysis analyze(Throwable failure);

}
