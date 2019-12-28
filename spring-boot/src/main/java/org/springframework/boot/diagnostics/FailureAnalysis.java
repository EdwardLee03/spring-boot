
package org.springframework.boot.diagnostics;

/**
 * The result of analyzing a failure.
 * 故障的分析结果。
 *
 * @author Andy Wilkinson
 * @since 1.4.0
 */
public class FailureAnalysis {

	/**
	 * 故障的描述
	 */
	private final String description;

	/**
	 * 用于解决故障的操作
	 */
	private final String action;

	/**
	 * 故障的原因
	 */
	private final Throwable cause;

	/**
	 * Creates a new {@code FailureAnalysis} with the given {@code description} and
	 * {@code action}, if any, that the user should take to address the problem. The
	 * failure had the given underlying {@code cause}.
	 * @param description the description
	 * @param action the action
	 * @param cause the cause
	 */
	public FailureAnalysis(String description, String action, Throwable cause) {
		this.description = description;
		this.action = action;
		this.cause = cause;
	}

	/**
	 * Returns a description of the failure.
	 * 返回故障的描述。
	 * @return the description
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * Returns the action, if any, to be taken to address the failure.
	 * 返回用于解决故障的操作，如果有。
	 * @return the action or {@code null}
	 */
	public String getAction() {
		return this.action;
	}

	/**
	 * Returns the cause of the failure.
	 * 返回故障的原因。
	 * @return the cause
	 */
	public Throwable getCause() {
		return this.cause;
	}

}
