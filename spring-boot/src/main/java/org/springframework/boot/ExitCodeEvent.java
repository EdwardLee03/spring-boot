
package org.springframework.boot;

import org.springframework.context.ApplicationEvent;

/**
 * Event fired when an application exit code has been determined from an
 * {@link ExitCodeGenerator}.
 * 从退出代码生成器确定应用程序退出代码后，触发这个事件。
 *
 * @author Phillip Webb
 * @since 1.3.2
 */
public class ExitCodeEvent extends ApplicationEvent {

	/**
	 * 用于退出JVM的退出代码
	 */
	private final int exitCode;

	/**
	 * Create a new {@link ExitCodeEvent} instance.
	 * @param source the source of the event
	 * @param exitCode the exit code
	 */
	public ExitCodeEvent(Object source, int exitCode) {
		super(source);
		this.exitCode = exitCode;
	}

	/**
	 * Return the exit code that will be used to exit the JVM.
	 * @return the exit code
	 */
	public int getExitCode() {
		return this.exitCode;
	}

}
