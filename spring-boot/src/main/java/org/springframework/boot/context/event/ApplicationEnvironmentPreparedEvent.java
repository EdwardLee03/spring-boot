
package org.springframework.boot.context.event;

import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

/**
 * Event published when a {@link SpringApplication} is starting up and the
 * {@link Environment} is first available for inspection and modification.
 * 在启动Spring应用且首次可用于检查和修改应用运行时环境时发布的事件。
 *
 * @author Dave Syer
 */
@SuppressWarnings("serial")
public class ApplicationEnvironmentPreparedEvent extends SpringApplicationEvent {

	/**
	 * 可配置的应用运行时环境
	 */
	private final ConfigurableEnvironment environment;

	/**
	 * Create a new {@link ApplicationEnvironmentPreparedEvent} instance.
	 * @param application the current application
	 * @param args the arguments the application is running with
	 * @param environment the environment that was just created
	 */
	public ApplicationEnvironmentPreparedEvent(SpringApplication application,
			String[] args, ConfigurableEnvironment environment) {
		super(application, args);
		this.environment = environment;
	}

	/**
	 * Return the environment.
	 * 返回可配置的应用运行时环境。
	 * @return the environment
	 */
	public ConfigurableEnvironment getEnvironment() {
		return this.environment;
	}

}
