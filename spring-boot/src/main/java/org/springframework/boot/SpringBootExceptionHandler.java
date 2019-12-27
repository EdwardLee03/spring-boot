
package org.springframework.boot;

import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * {@link UncaughtExceptionHandler} to suppress handling already logged exceptions and
 * dealing with system exit.
 * 一种未捕获的异常处理器，禁止处理已记录的异常和处理系统退出。
 *
 * @author Phillip Webb
 */
class SpringBootExceptionHandler implements UncaughtExceptionHandler {

	private static Set<String> LOG_CONFIGURATION_MESSAGES;

	static {
		Set<String> messages = new HashSet<String>();
		messages.add("Logback configuration error detected");
		LOG_CONFIGURATION_MESSAGES = Collections.unmodifiableSet(messages);
	}

	/**
	 * 已记录的异常处理器的线程本地变量
	 */
	private static LoggedExceptionHandlerThreadLocal handler = new LoggedExceptionHandlerThreadLocal();

	/**
	 * 未捕获的异常处理器
	 */
	private final UncaughtExceptionHandler parent;

	/**
	 * 已记录的异常列表
	 */
	private final List<Throwable> loggedExceptions = new ArrayList<Throwable>();

	/**
	 * 用于退出JVM的退出代码
	 */
	private int exitCode = 0;

	SpringBootExceptionHandler(UncaughtExceptionHandler parent) {
		this.parent = parent;
	}

	public void registerLoggedException(Throwable exception) {
		this.loggedExceptions.add(exception);
	}

	public void registerExitCode(int exitCode) {
		this.exitCode = exitCode;
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		try {
			if (isPassedToParent(ex) && this.parent != null) {
				this.parent.uncaughtException(thread, ex);
			}
		}
		finally {
			this.loggedExceptions.clear();
			if (this.exitCode != 0) {
				System.exit(this.exitCode);
			}
		}
	}

	private boolean isPassedToParent(Throwable ex) {
		return isLogConfigurationMessage(ex) || !isRegistered(ex);
	}

	/**
	 * Check if the exception is a log configuration message, i.e. the log call might not
	 * have actually output anything.
	 * @param ex the source exception
	 * @return {@code true} if the exception contains a log configuration message
	 */
	private boolean isLogConfigurationMessage(Throwable ex) {
		if (ex instanceof InvocationTargetException) {
			return isLogConfigurationMessage(ex.getCause());
		}
		String message = ex.getMessage();
		if (message != null) {
			for (String candidate : LOG_CONFIGURATION_MESSAGES) {
				if (message.contains(candidate)) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean isRegistered(Throwable ex) {
		if (this.loggedExceptions.contains(ex)) {
			return true;
		}
		if (ex instanceof InvocationTargetException) {
			return isRegistered(ex.getCause());
		}
		return false;
	}

	static SpringBootExceptionHandler forCurrentThread() {
		return handler.get();
	}

	/**
	 * Thread local used to attach and track handlers.
	 */
	private static class LoggedExceptionHandlerThreadLocal
			extends ThreadLocal<SpringBootExceptionHandler> {

		@Override
		protected SpringBootExceptionHandler initialValue() {
			// 当前执行线程的未捕获的异常处理器
			SpringBootExceptionHandler handler = new SpringBootExceptionHandler(
					Thread.currentThread().getUncaughtExceptionHandler());
			// 将boot异常处理器设置为当前执行线程的未捕获的异常处理器
			Thread.currentThread().setUncaughtExceptionHandler(handler);
			return handler;
		}

	}

}
