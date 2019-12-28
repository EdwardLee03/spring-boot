
package org.springframework.boot.context;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;

/**
 * An {@link ApplicationListener} that halts application startup if the system file
 * encoding does not match an expected value set in the environment. By default has no
 * effect, but if you set {@code spring.mandatory_file_encoding} (or some camelCase or
 * UPPERCASE variant of that) to the name of a character encoding (e.g. "UTF-8") then this
 * initializer throws an exception when the {@code file.encoding} System property does not
 * equal it.
 * 如果系统文件编码与应用运行时环境中设置的期望值不匹配，则应用监视器会停止应用程序启动。
 * 默认情况下没有任何作用，但是如果将spring.mandatory_file_encoding设置为字符编码的名称，
 * 则当file.encoding系统属性不等于它时，这个初始化器将抛出异常。
 *
 * <p>
 * The System property {@code file.encoding} is normally set by the JVM in response to the
 * {@code LANG} or {@code LC_ALL} environment variables. It is used (along with other
 * platform-dependent variables keyed off those environment variables) to encode JVM
 * arguments as well as file names and paths. In most cases you can override the file
 * encoding System property on the command line (with standard JVM features), but also
 * consider setting the {@code LANG} environment variable to an explicit
 * character-encoding value (e.g. "en_GB.UTF-8").
 * 系统属性file.encoding通常由JVM设置，以响应LANG或LC_ALL环境变量，用于编码JVM参数以及文件名和路径。
 * 在大多数情况下，可以在命令行上覆盖文件编码的系统属性，但也可以考虑将LANG环境变量设置为显式的字符编码值。
 *
 * @author Dave Syer
 */
public class FileEncodingApplicationListener
		implements ApplicationListener<ApplicationEnvironmentPreparedEvent>, Ordered {

	private static final Log logger = LogFactory
			.getLog(FileEncodingApplicationListener.class);

	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE;
	}

	@Override
	public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
		// 松散的属性解析器
		RelaxedPropertyResolver resolver = new RelaxedPropertyResolver(
				event.getEnvironment(), "spring.");
		if (resolver.containsProperty("mandatoryFileEncoding")) {
			// file.encoding系统属性
			String encoding = System.getProperty("file.encoding");
			String desired = resolver.getProperty("mandatoryFileEncoding");
			if (encoding != null && !desired.equalsIgnoreCase(encoding)) {
				// 前提条件：配置了spring.mandatoryFileEncoding
				// 系统文件编码与应用运行时环境中设置的期望值不匹配
				logger.error("System property 'file.encoding' is currently '" + encoding
						+ "'. It should be '" + desired
						+ "' (as defined in 'spring.mandatoryFileEncoding').");
				// LANG或LC_ALL环境变量
				logger.error("Environment variable LANG is '" + System.getenv("LANG")
						+ "'. You could use a locale setting that matches encoding='"
						+ desired + "'.");
				logger.error("Environment variable LC_ALL is '" + System.getenv("LC_ALL")
						+ "'. You could use a locale setting that matches encoding='"
						+ desired + "'.");
				// 抛出非法状态运行时异常
				throw new IllegalStateException(
						// JVM尚未配置为使用所需的默认字符编码
						"The Java Virtual Machine has not been configured to use the "
								+ "desired default character encoding (" + desired
								+ ").");
			}
		}
	}

}
