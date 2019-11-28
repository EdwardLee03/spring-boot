
package org.springframework.boot;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;

import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;

/**
 * Logs application information on startup.
 * 记录应用的启动信息。
 *
 * @author Phillip Webb
 * @author Dave Syer
 */
class StartupInfoLogger {

	/**
	 * 应用主类
	 */
	private final Class<?> sourceClass;

	StartupInfoLogger(Class<?> sourceClass) {
		this.sourceClass = sourceClass;
	}

	// 记录信息

	/**
	 * 记录应用启动信息。
	 */
	void logStarting(Log log) {
		Assert.notNull(log, "Log must not be null");
		if (log.isInfoEnabled()) {
			log.info(getStartupMessage());
		}
		if (log.isDebugEnabled()) {
			log.debug(getRunningMessage());
		}
	}

	/**
	 * 记录应用启动完成信息。
	 */
	void logStarted(Log log, StopWatch stopWatch) {
		if (log.isInfoEnabled()) {
			log.info(getStartedMessage(stopWatch));
		}
	}

	// 生成信息

	/**
	 * 应用启动信息。
	 * <pre>
	 * 格式：
	 * "Starting {ApplicationName} {Version} {On} {Pid} {Context}"
	 * </pre>
	 */
	private String getStartupMessage() {
		return "Starting " +
				getApplicationName() +
				getVersion(this.sourceClass) +
				getOn() +
				getPid() +
				getContext();
	}

	/**
	 * 应用运行信息。
	 * <pre>
	 * 格式：
	 * "Running with Spring Boot {Version}, Spring {ApplicationContextVersion}"
	 * </pre>
	 */
	private String getRunningMessage() {
		return "Running with Spring Boot" +
				getVersion(this.sourceClass) +
				", Spring" +
				getVersion(ApplicationContext.class);
	}

	/**
	 * 应用启动完成信息。
	 * <pre>
	 * 格式：
	 * "Started {ApplicationName} in {TotalTimeSeconds} seconds (JVM running for {uptime})"
	 * </pre>
	 */
	private String getStartedMessage(StopWatch stopWatch) {
		StringBuilder message = new StringBuilder();
		message.append("Started ");
		message.append(getApplicationName());
		message.append(" in ");
		message.append(stopWatch.getTotalTimeSeconds());
		try {
			double uptime = ManagementFactory.getRuntimeMXBean().getUptime() / 1000.0;
			message.append(" seconds (JVM running for ").append(uptime).append(")");
		}
		catch (Exception ex) {
			// No JVM time available
		}
		return message.toString();
	}

	// 应用信息

	/**
	 * 应用名称。
	 * <pre>
	 * 格式："ClassUtils.getShortName(sourceClass)/application"
	 * </pre>
	 */
	private String getApplicationName() {
		// 应用主类的短名称
		return (this.sourceClass != null) ? ClassUtils.getShortName(this.sourceClass)
				: "application";
	}

	/**
	 * 应用主类的jar版本。
	 * <pre>
	 * 格式：" v{sourceClassImplementationVersion}"
	 * </pre>
	 */
	private String getVersion(final Class<?> source) {
		return getValue(" v", new Callable<Object>() {
			@Override
			public Object call() {
				return source.getPackage().getImplementationVersion();
			}
		});
	}

	/**
	 * 主机名称。
	 * <pre>
	 * 格式：" on {LocalHost.HostName}"
	 * </pre>
	 */
	private String getOn() {
		return getValue(" on ", new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				return InetAddress.getLocalHost().getHostName();
			}
		});
	}

	/**
	 * 进程id。
	 * <pre>
	 * 格式：" with PID {PID}"
	 * </pre>
	 */
	private String getPid() {
		return getValue(" with PID ", new Callable<Object>() {
			@Override
			public Object call() {
				// 通过系统属性传递
				return System.getProperty("PID");
			}
		});
	}

	/**
	 * 应用上下文。
	 * <pre>
	 * 格式：" ({sourceClassAbsolutePath} started by {user.name} in {user.dir})"
	 * </pre>
	 */
	private String getContext() {
		String startedBy = getValue("started by ", new Callable<Object>() {
			@Override
			public Object call() {
				return System.getProperty("user.name");
			}
		});
		String in = getValue("in ", new Callable<Object>() {
			@Override
			public Object call() {
				return System.getProperty("user.dir");
			}
		});
		ApplicationHome home = new ApplicationHome(this.sourceClass);
		String path = (home.getSource() != null) ? home.getSource().getAbsolutePath()
				: "";
		if (startedBy.isEmpty() && path.isEmpty()) {
			return "";
		}
		if (StringUtils.hasLength(startedBy) && StringUtils.hasLength(path)) {
			startedBy = " " + startedBy;
		}
		if (StringUtils.hasLength(in) && StringUtils.hasLength(startedBy)) {
			in = " " + in;
		}
		return " (" + path + startedBy + in + ")";
	}

	private String getValue(String prefix, Callable<Object> call) {
		return getValue(prefix, call, "");
	}

	private String getValue(String prefix, Callable<Object> call, String defaultValue) {
		try {
			Object value = call.call();
			if (value != null && StringUtils.hasLength(value.toString())) {
				return prefix + value;
			}
		}
		catch (Exception ex) {
			// Swallow and continue
		}
		return defaultValue;
	}

}
