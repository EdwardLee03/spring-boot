
package org.springframework.boot.diagnostics;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.util.StringUtils;

/**
 * {@link FailureAnalysisReporter} that logs the failure analysis.
 * 通过日志记录故障分析结果的报告者。
 *
 * @author Andy Wilkinson
 * @since 1.4.0
 */
public final class LoggingFailureAnalysisReporter implements FailureAnalysisReporter {

	private static final Log logger = LogFactory
			.getLog(LoggingFailureAnalysisReporter.class);

	@Override
	public void report(FailureAnalysis failureAnalysis) {
		if (logger.isDebugEnabled()) {
			// 应用启动失败的异常
			logger.debug("Application failed to start due to an exception",
					failureAnalysis.getCause());
		}
		if (logger.isErrorEnabled()) {
			// 记录故障分析结果的错误日志
			logger.error(buildMessage(failureAnalysis));
		}
	}

	private String buildMessage(FailureAnalysis failureAnalysis) {
		StringBuilder builder = new StringBuilder();
		builder.append(String.format("%n%n"));
		builder.append(String.format("***************************%n"));
		// 应用程序无法启动
		builder.append(String.format("APPLICATION FAILED TO START%n"));
		builder.append(String.format("***************************%n%n"));
		// 故障的描述
		builder.append(String.format("Description:%n%n"));
		builder.append(String.format("%s%n", failureAnalysis.getDescription()));
		if (StringUtils.hasText(failureAnalysis.getAction())) {
			// 用于解决故障的操作
			builder.append(String.format("%nAction:%n%n"));
			builder.append(String.format("%s%n", failureAnalysis.getAction()));
		}
		return builder.toString();
	}

}
