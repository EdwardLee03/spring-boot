
package org.springframework.boot.diagnostics.analyzer;

import org.springframework.boot.context.embedded.PortInUseException;
import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;

/**
 * A {@code FailureAnalyzer} that performs analysis of failures caused by a
 * {@code PortInUseException}.
 * 用于分析由端口已在使用中异常引起的故障。
 *
 * @author Andy Wilkinson
 */
class PortInUseFailureAnalyzer extends AbstractFailureAnalyzer<PortInUseException> {

	@Override
	protected FailureAnalysis analyze(Throwable rootFailure, PortInUseException cause) {
		return new FailureAnalysis(
				"Embedded servlet container failed to start. Port " + cause.getPort()
						+ " was already in use.",
				"Identify and stop the process that's listening on port "
						+ cause.getPort() + " or configure this "
						+ "application to listen on another port.",
				cause);
	}

}
