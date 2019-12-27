
package org.springframework.boot;

/**
 * Strategy interface that can be used to provide a mapping between exceptions and exit
 * codes.
 * 策略接口，可用于提供异常和退出代码之间的映射。
 *
 * @author Phillip Webb
 * @since 1.3.2
 */
public interface ExitCodeExceptionMapper {

	/**
	 * Returns the exit code that should be returned from the application.
	 * @param exception the exception causing the application to exit
	 * @return the exit code or {@code 0}.
	 */
	int getExitCode(Throwable exception);

}
